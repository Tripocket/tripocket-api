package pl.tripocket.tripocket_api.expense;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import pl.tripocket.tripocket_api.auth.user.model.User;
import pl.tripocket.tripocket_api.auth.user.repository.UserRepository;
import pl.tripocket.tripocket_api.common.exception.ResourceNotFoundException;
import pl.tripocket.tripocket_api.expense.dto.ExpenseCreateRequest;
import pl.tripocket.tripocket_api.expense.dto.ExpenseResponse;
import pl.tripocket.tripocket_api.expense.dto.ExpenseSplitRequest;
import pl.tripocket.tripocket_api.expense.model.Expense;
import pl.tripocket.tripocket_api.expense.model.ExpenseSplit;
import pl.tripocket.tripocket_api.expense.model.RateSource;
import pl.tripocket.tripocket_api.expense.repository.ExpenseRepository;
import pl.tripocket.tripocket_api.expense.service.ExpenseService;
import pl.tripocket.tripocket_api.trip.model.Trip;
import pl.tripocket.tripocket_api.trip.model.TripParticipant;
import pl.tripocket.tripocket_api.trip.model.TripRole;
import pl.tripocket.tripocket_api.trip.repository.TripRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceTest {

    @Mock private ExpenseRepository expenseRepository;
    @Mock private TripRepository tripRepository;
    @Mock private UserRepository userRepository;
    @Mock private JwtAuthenticationToken token;

    @InjectMocks private ExpenseService expenseService;

    private UUID ownerId;
    private UUID participantId;
    private User owner;
    private User participant;
    private Trip trip;
    private UUID tripId;

    @BeforeEach
    void setUp() {
        ownerId = UUID.randomUUID();
        participantId = UUID.randomUUID();

        owner = newUser(ownerId, "owner");
        participant = newUser(participantId, "participant");

        tripId = UUID.randomUUID();
        trip = new Trip();
        trip.setId(tripId);
        trip.setParticipants(new ArrayList<>(List.of(
                TripParticipant.builder().user(owner).role(TripRole.OWNER).build(),
                TripParticipant.builder().user(participant).role(TripRole.PARTICIPANT).build()
        )));
    }

    private User newUser(UUID id, String username) {
        User u = new User();
        u.setId(id);
        u.setUsername(username);
        u.setEmail(username + "@example.com");
        u.setDisplayName(username);
        return u;
    }

    private ExpenseCreateRequest request(BigDecimal amount, List<ExpenseSplitRequest> splits) {
        return new ExpenseCreateRequest(
                "Jedzenie", amount, "EUR", new BigDecimal("4.300000"), RateSource.MANUAL,
                LocalDate.of(2024, 7, 5), "Kolacja", ownerId, splits
        );
    }

    private void stubCurrentUser(UUID userId) {
        when(token.getName()).thenReturn(userId.toString());
    }

    private void stubUserLookups() {
        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(userRepository.findAllById(anyIterable())).thenAnswer(inv -> {
            Iterable<UUID> ids = inv.getArgument(0);
            Map<UUID, User> all = Map.of(ownerId, owner, participantId, participant);
            List<User> result = new ArrayList<>();
            ids.forEach(id -> result.add(all.get(id)));
            return result;
        });
    }

    @Test
    void createExpense_EqualSplit_DistributesOwedAmountsAndHandlesRounding() {
        stubCurrentUser(ownerId);
        when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));
        stubUserLookups();
        when(expenseRepository.save(any(Expense.class))).thenAnswer(inv -> {
            Expense e = inv.getArgument(0);
            e.setId(UUID.randomUUID());
            return e;
        });

        // 100.00 / 2 uczestników, bez podanego share -> równy podział
        ExpenseCreateRequest req = request(new BigDecimal("100.01"), List.of(
                new ExpenseSplitRequest(ownerId, null),
                new ExpenseSplitRequest(participantId, null)
        ));

        ExpenseResponse response = expenseService.createExpense(tripId, req, token);

        ArgumentCaptor<Expense> captor = ArgumentCaptor.forClass(Expense.class);
        verify(expenseRepository).save(captor.capture());
        List<ExpenseSplit> splits = captor.getValue().getSplits();

        assertEquals(2, splits.size());
        BigDecimal total = splits.stream()
                .map(ExpenseSplit::getOwedAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        // Suma długów musi się równać kwocie wydatku
        assertEquals(0, new BigDecimal("100.01").compareTo(total));
        // Reszta z zaokrąglenia trafia do pierwszego uczestnika
        assertEquals(0, new BigDecimal("50.01").compareTo(splits.get(0).getOwedAmount()));
        assertEquals(0, new BigDecimal("50.00").compareTo(splits.get(1).getOwedAmount()));
        assertEquals(2, response.splits().size());
    }

    @Test
    void createExpense_StoresFrozenExchangeRateAndSource() {
        stubCurrentUser(ownerId);
        when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));
        stubUserLookups();
        when(expenseRepository.save(any(Expense.class))).thenAnswer(inv -> inv.getArgument(0));

        ExpenseCreateRequest req = request(new BigDecimal("100.00"), List.of(
                new ExpenseSplitRequest(ownerId, null),
                new ExpenseSplitRequest(participantId, null)
        ));

        ExpenseResponse response = expenseService.createExpense(tripId, req, token);

        ArgumentCaptor<Expense> captor = ArgumentCaptor.forClass(Expense.class);
        verify(expenseRepository).save(captor.capture());
        assertEquals(0, new BigDecimal("4.300000").compareTo(captor.getValue().getExchangeRate()));
        assertEquals(RateSource.MANUAL, captor.getValue().getRateSource());
        assertEquals(0, new BigDecimal("4.300000").compareTo(response.exchangeRate()));
        assertEquals(RateSource.MANUAL, response.rateSource());
    }

    @Test
    void createExpense_CustomShares_StoresProvidedOwedAmounts() {
        stubCurrentUser(ownerId);
        when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));
        stubUserLookups();
        when(expenseRepository.save(any(Expense.class))).thenAnswer(inv -> inv.getArgument(0));

        ExpenseCreateRequest req = request(new BigDecimal("100.00"), List.of(
                new ExpenseSplitRequest(ownerId, new BigDecimal("70.00")),
                new ExpenseSplitRequest(participantId, new BigDecimal("30.00"))
        ));

        expenseService.createExpense(tripId, req, token);

        ArgumentCaptor<Expense> captor = ArgumentCaptor.forClass(Expense.class);
        verify(expenseRepository).save(captor.capture());
        Map<UUID, BigDecimal> owedByUser = captor.getValue().getSplits().stream()
                .collect(Collectors.toMap(s -> s.getUser().getId(), ExpenseSplit::getOwedAmount));

        assertEquals(0, new BigDecimal("70.00").compareTo(owedByUser.get(ownerId)));
        assertEquals(0, new BigDecimal("30.00").compareTo(owedByUser.get(participantId)));
    }

    @Test
    void createExpense_Fails_WhenSharesDoNotSumToAmount() {
        stubCurrentUser(ownerId);
        when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));

        ExpenseCreateRequest req = request(new BigDecimal("100.00"), List.of(
                new ExpenseSplitRequest(ownerId, new BigDecimal("70.00")),
                new ExpenseSplitRequest(participantId, new BigDecimal("40.00"))
        ));

        assertThrows(IllegalArgumentException.class,
                () -> expenseService.createExpense(tripId, req, token));
        verify(expenseRepository, never()).save(any());
    }

    @Test
    void createExpense_Fails_WhenSharePartiallyProvided() {
        stubCurrentUser(ownerId);
        when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));

        ExpenseCreateRequest req = request(new BigDecimal("100.00"), List.of(
                new ExpenseSplitRequest(ownerId, new BigDecimal("70.00")),
                new ExpenseSplitRequest(participantId, null)
        ));

        assertThrows(IllegalArgumentException.class,
                () -> expenseService.createExpense(tripId, req, token));
        verify(expenseRepository, never()).save(any());
    }

    @Test
    void createExpense_Fails_WhenPayerNotTripParticipant() {
        stubCurrentUser(ownerId);
        when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));

        UUID outsiderId = UUID.randomUUID();
        ExpenseCreateRequest req = new ExpenseCreateRequest(
                "Jedzenie", new BigDecimal("100.00"), "EUR", new BigDecimal("4.300000"), RateSource.MANUAL,
                LocalDate.of(2024, 7, 5), "Kolacja", outsiderId,
                List.of(new ExpenseSplitRequest(ownerId, null))
        );

        assertThrows(IllegalArgumentException.class,
                () -> expenseService.createExpense(tripId, req, token));
        verify(expenseRepository, never()).save(any());
    }

    @Test
    void createExpense_Fails_WhenSplitUserNotTripParticipant() {
        stubCurrentUser(ownerId);
        when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));

        UUID outsiderId = UUID.randomUUID();
        ExpenseCreateRequest req = request(new BigDecimal("100.00"), List.of(
                new ExpenseSplitRequest(ownerId, null),
                new ExpenseSplitRequest(outsiderId, null)
        ));

        assertThrows(IllegalArgumentException.class,
                () -> expenseService.createExpense(tripId, req, token));
        verify(expenseRepository, never()).save(any());
    }

    @Test
    void createExpense_Fails_WhenDuplicateSplitUsers() {
        stubCurrentUser(ownerId);
        when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));

        ExpenseCreateRequest req = request(new BigDecimal("100.00"), List.of(
                new ExpenseSplitRequest(ownerId, null),
                new ExpenseSplitRequest(ownerId, null)
        ));

        assertThrows(IllegalArgumentException.class,
                () -> expenseService.createExpense(tripId, req, token));
        verify(expenseRepository, never()).save(any());
    }

    @Test
    void createExpense_Fails_WhenCurrentUserNotTripParticipant() {
        UUID outsiderId = UUID.randomUUID();
        stubCurrentUser(outsiderId);
        when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));

        ExpenseCreateRequest req = request(new BigDecimal("100.00"), List.of(
                new ExpenseSplitRequest(ownerId, null)
        ));

        assertThrows(AccessDeniedException.class,
                () -> expenseService.createExpense(tripId, req, token));
    }

    @Test
    void deleteExpense_Succeeds_WhenTripOwner() {
        stubCurrentUser(ownerId);
        Expense expense = expensePaidBy(participant);
        when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));
        when(expenseRepository.findById(expense.getId())).thenReturn(Optional.of(expense));

        expenseService.deleteExpense(tripId, expense.getId(), token);

        verify(expenseRepository).delete(expense);
    }

    @Test
    void deleteExpense_Succeeds_WhenExpenseOwner() {
        // Uczestnik (nie właściciel podróży), ale zapłacił za wydatek
        stubCurrentUser(participantId);
        Expense expense = expensePaidBy(participant);
        when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));
        when(expenseRepository.findById(expense.getId())).thenReturn(Optional.of(expense));

        expenseService.deleteExpense(tripId, expense.getId(), token);

        verify(expenseRepository).delete(expense);
    }

    @Test
    void deleteExpense_Fails_WhenNotOwnerNorPayer() {
        // Uczestnik próbuje usunąć cudzy wydatek (zapłacony przez właściciela)
        stubCurrentUser(participantId);
        Expense expense = expensePaidBy(owner);
        when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));
        when(expenseRepository.findById(expense.getId())).thenReturn(Optional.of(expense));

        assertThrows(AccessDeniedException.class,
                () -> expenseService.deleteExpense(tripId, expense.getId(), token));
        verify(expenseRepository, never()).delete(any());
    }

    @Test
    void deleteExpense_Fails_WhenPayerNoLongerTripParticipant() {
        UUID formerMemberId = UUID.randomUUID();
        User formerMember = newUser(formerMemberId, "former");
        stubCurrentUser(formerMemberId);
        Expense expense = expensePaidBy(formerMember);
        when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));

        assertThrows(AccessDeniedException.class,
                () -> expenseService.deleteExpense(tripId, expense.getId(), token));
        verify(expenseRepository, never()).delete(any());
    }

    @Test
    void deleteExpense_Fails_WhenExpenseBelongsToAnotherTrip() {
        stubCurrentUser(ownerId);
        Trip otherTrip = new Trip();
        otherTrip.setId(UUID.randomUUID());
        Expense expense = new Expense();
        expense.setId(UUID.randomUUID());
        expense.setTrip(otherTrip);
        expense.setPaidBy(owner);

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));
        when(expenseRepository.findById(expense.getId())).thenReturn(Optional.of(expense));

        assertThrows(ResourceNotFoundException.class,
                () -> expenseService.deleteExpense(tripId, expense.getId(), token));
        verify(expenseRepository, never()).delete(any());
    }

    private Expense expensePaidBy(User payer) {
        Expense expense = new Expense();
        expense.setId(UUID.randomUUID());
        expense.setTrip(trip);
        expense.setPaidBy(payer);
        return expense;
    }
}
