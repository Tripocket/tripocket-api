package pl.tripocket.tripocket_api.expense.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.tripocket.tripocket_api.auth.user.model.User;
import pl.tripocket.tripocket_api.auth.user.repository.UserRepository;
import pl.tripocket.tripocket_api.common.exception.ResourceNotFoundException;
import pl.tripocket.tripocket_api.expense.dto.*;
import pl.tripocket.tripocket_api.expense.model.Expense;
import pl.tripocket.tripocket_api.expense.model.ExpenseSplit;
import pl.tripocket.tripocket_api.expense.repository.ExpenseRepository;
import pl.tripocket.tripocket_api.trip.model.Trip;
import pl.tripocket.tripocket_api.trip.model.TripParticipant;
import pl.tripocket.tripocket_api.trip.model.TripRole;
import pl.tripocket.tripocket_api.trip.repository.TripRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private static final BigDecimal CENT = new BigDecimal("0.01");

    private final ExpenseRepository expenseRepository;
    private final TripRepository tripRepository;
    private final UserRepository userRepository;

    @Transactional
    public ExpenseResponse createExpense(UUID tripId, ExpenseCreateRequest request, JwtAuthenticationToken token) {
        Trip trip = getTripIfCurrentUserIsParticipant(tripId, token);

        Set<UUID> tripParticipantIds = getTripParticipantIds(trip);

        if (!tripParticipantIds.contains(request.paidByUserId())) {
            throw new IllegalArgumentException("Użytkownik wskazany jako płacący nie jest uczestnikiem tej podróży.");
        }

        List<ExpenseSplitRequest> splitRequests = request.splits();
        Set<UUID> splitUserIds = splitRequests.stream()
                .map(ExpenseSplitRequest::userId)
                .collect(Collectors.toCollection(HashSet::new));

        if (splitUserIds.size() != splitRequests.size()) {
            throw new IllegalArgumentException("Lista podziału wydatku zawiera duplikaty użytkowników.");
        }

        if (!tripParticipantIds.containsAll(splitUserIds)) {
            throw new IllegalArgumentException("Wszyscy uczestnicy podziału muszą być uczestnikami podróży.");
        }

        List<BigDecimal> owedAmounts = calculateOwedAmounts(splitRequests, request.amount());

        User paidBy = userRepository.findById(request.paidByUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Użytkownik płacący nie istnieje."));

        List<User> users = userRepository.findAllById(splitUserIds);
        if (users.size() != splitUserIds.size()) {
            throw new ResourceNotFoundException("Jeden lub więcej uczestników podziału nie istnieje.");
        }
        Map<UUID, User> userById = users.stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        Expense expense = new Expense();
        expense.setTrip(trip);
        expense.setCategory(request.category());
        expense.setAmount(request.amount());
        expense.setCurrency(request.currency().toUpperCase());
        expense.setExpenseDate(request.expenseDate());
        expense.setDescription(request.description());
        expense.setPaidBy(paidBy);

        for (int i = 0; i < splitRequests.size(); i++) {
            ExpenseSplit split = ExpenseSplit.builder()
                    .expense(expense)
                    .user(userById.get(splitRequests.get(i).userId()))
                    .owedAmount(owedAmounts.get(i))
                    .build();
            expense.getSplits().add(split);
        }

        Expense saved = expenseRepository.save(expense);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponse> getTripExpenses(UUID tripId, JwtAuthenticationToken token) {
        getTripIfCurrentUserIsParticipant(tripId, token);

        return expenseRepository.findAllByTripId(tripId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ExpenseResponse getExpense(UUID tripId, UUID expenseId, JwtAuthenticationToken token) {
        getTripIfCurrentUserIsParticipant(tripId, token);

        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new ResourceNotFoundException("Wydatek nie istnieje."));

        if (!expense.getTrip().getId().equals(tripId)) {
            throw new ResourceNotFoundException("Wydatek nie należy do wskazanej podróży.");
        }

        return toResponse(expense);
    }

    @Transactional
    public void deleteExpense(UUID tripId, UUID expenseId, JwtAuthenticationToken token) {
        UUID currentUserId = UUID.fromString(token.getName());

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Podróż nie istnieje."));

        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new ResourceNotFoundException("Wydatek nie istnieje."));

        if (!expense.getTrip().getId().equals(tripId)) {
            throw new ResourceNotFoundException("Wydatek nie należy do wskazanej podróży.");
        }

        boolean isTripOwner = trip.getParticipants().stream()
                .anyMatch(p -> p.getUser().getId().equals(currentUserId) && p.getRole() == TripRole.OWNER);
        boolean isExpenseOwner = expense.getPaidBy().getId().equals(currentUserId);

        if (!isTripOwner && !isExpenseOwner) {
            throw new AccessDeniedException("Wydatek może usunąć jedynie właściciel podróży lub osoba, która za niego zapłaciła.");
        }

        expenseRepository.delete(expense);
    }

    private List<BigDecimal> calculateOwedAmounts(List<ExpenseSplitRequest> splits, BigDecimal amount) {
        boolean anyShare = splits.stream().anyMatch(s -> s.share() != null);
        boolean allShares = splits.stream().allMatch(s -> s.share() != null);

        if (anyShare && !allShares) {
            throw new IllegalArgumentException("Udział (share) musi być podany dla wszystkich uczestników albo dla żadnego.");
        }

        if (allShares) {
            BigDecimal sum = splits.stream()
                    .map(ExpenseSplitRequest::share)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            if (sum.compareTo(amount) != 0) {
                throw new IllegalArgumentException("Suma udziałów (share) musi być równa kwocie wydatku.");
            }
            return splits.stream()
                    .map(s -> s.share().setScale(2, RoundingMode.HALF_UP))
                    .collect(Collectors.toList());
        }

        int count = splits.size();
        BigDecimal base = amount.setScale(2, RoundingMode.DOWN)
                .divide(BigDecimal.valueOf(count), 2, RoundingMode.DOWN);

        List<BigDecimal> owed = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            owed.add(base);
        }

        BigDecimal remainder = amount.setScale(2, RoundingMode.HALF_UP)
                .subtract(base.multiply(BigDecimal.valueOf(count)));
        for (int i = 0; remainder.compareTo(BigDecimal.ZERO) > 0 && i < count; i++) {
            owed.set(i, owed.get(i).add(CENT));
            remainder = remainder.subtract(CENT);
        }

        return owed;
    }

    private Trip getTripIfCurrentUserIsParticipant(UUID tripId, JwtAuthenticationToken token) {
        UUID currentUserId = UUID.fromString(token.getName());

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Podróż nie istnieje."));

        boolean isParticipant = trip.getParticipants().stream()
                .anyMatch(p -> p.getUser().getId().equals(currentUserId));

        if (!isParticipant) {
            throw new AccessDeniedException("Brak dostępu do tej podróży.");
        }

        return trip;
    }

    private Set<UUID> getTripParticipantIds(Trip trip) {
        return trip.getParticipants().stream()
                .map(TripParticipant::getUser)
                .map(User::getId)
                .collect(Collectors.toSet());
    }

    private ExpenseResponse toResponse(Expense expense) {
        return new ExpenseResponse(
                expense.getId(),
                expense.getTrip().getId(),
                expense.getCategory(),
                expense.getAmount(),
                expense.getCurrency(),
                expense.getExpenseDate(),
                expense.getDescription(),
                toParticipantResponse(expense.getPaidBy()),
                expense.getSplits().stream()
                        .map(this::toSplitResponse)
                        .toList(),
                expense.getCreatedAt()
        );
    }

    private ExpenseSplitResponse toSplitResponse(ExpenseSplit split) {
        return new ExpenseSplitResponse(
                toParticipantResponse(split.getUser()),
                split.getOwedAmount()
        );
    }

    private ExpenseParticipantResponse toParticipantResponse(User user) {
        return new ExpenseParticipantResponse(
                user.getId(),
                user.getUsername(),
                user.getDisplayName(),
                user.getEmail()
        );
    }
}
