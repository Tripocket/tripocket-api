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
import pl.tripocket.tripocket_api.expense.model.ExpenseParticipant;
import pl.tripocket.tripocket_api.expense.repository.ExpenseRepository;
import pl.tripocket.tripocket_api.trip.model.Trip;
import pl.tripocket.tripocket_api.trip.model.TripParticipant;
import pl.tripocket.tripocket_api.trip.repository.TripRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ExpenseService {

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

        Set<UUID> requestedParticipantIds = new HashSet<>(request.participantUserIds());

        if (requestedParticipantIds.size() != request.participantUserIds().size()) {
            throw new IllegalArgumentException("Lista uczestników wydatku zawiera duplikaty.");
        }

        if (!tripParticipantIds.containsAll(requestedParticipantIds)) {
            throw new IllegalArgumentException("Wszyscy uczestnicy wydatku muszą być uczestnikami podróży.");
        }

        User paidBy = userRepository.findById(request.paidByUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Użytkownik płacący nie istnieje."));

        List<User> participants = userRepository.findAllById(requestedParticipantIds);

        if (participants.size() != requestedParticipantIds.size()) {
            throw new ResourceNotFoundException("Jeden lub więcej uczestników wydatku nie istnieje.");
        }

        Expense expense = new Expense();
        expense.setTrip(trip);
        expense.setCategory(request.category());
        expense.setAmount(request.amount());
        expense.setCurrency(request.currency().toUpperCase());
        expense.setExpenseDate(request.expenseDate());
        expense.setDescription(request.description());
        expense.setPaidBy(paidBy);

        for (User user : participants) {
            ExpenseParticipant participant = ExpenseParticipant.builder()
                    .expense(expense)
                    .user(user)
                    .build();

            expense.getParticipants().add(participant);
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
        getTripIfCurrentUserIsParticipant(tripId, token);

        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new ResourceNotFoundException("Wydatek nie istnieje."));

        if (!expense.getTrip().getId().equals(tripId)) {
            throw new ResourceNotFoundException("Wydatek nie należy do wskazanej podróży.");
        }

        expenseRepository.delete(expense);
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
                .collect(java.util.stream.Collectors.toSet());
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
                expense.getParticipants().stream()
                        .map(ExpenseParticipant::getUser)
                        .map(this::toParticipantResponse)
                        .toList(),
                expense.getCreatedAt()
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