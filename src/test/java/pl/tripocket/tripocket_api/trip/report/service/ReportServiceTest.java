package pl.tripocket.tripocket_api.trip.report.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.tripocket.tripocket_api.common.exception.ResourceNotFoundException;
import pl.tripocket.tripocket_api.trip.model.Trip;
import pl.tripocket.tripocket_api.trip.report.dto.CategoryReportResponse;
import pl.tripocket.tripocket_api.trip.report.dto.SettlementReportResponse;
import pl.tripocket.tripocket_api.trip.repository.ExpenseRepository;
import pl.tripocket.tripocket_api.trip.repository.ExpenseRepository.CategoryTotal;
import pl.tripocket.tripocket_api.trip.repository.ExpenseSplitRepository;
import pl.tripocket.tripocket_api.trip.repository.ExpenseSplitRepository.DebtEntry;
import pl.tripocket.tripocket_api.trip.repository.TripRepository;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

  // Fixed UUIDs so UUID ordering in netting is predictable: USER_A < USER_B
  private static final UUID TRIP_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
  private static final UUID USER_A = UUID.fromString("00000000-0000-0000-0000-000000000010");
  private static final UUID USER_B = UUID.fromString("00000000-0000-0000-0000-000000000020");

  @Mock TripRepository tripRepository;
  @Mock ExpenseRepository expenseRepository;
  @Mock ExpenseSplitRepository expenseSplitRepository;

  @InjectMocks ReportService reportService;

  // ==== getCategoryReport

  @Test
  void getCategoryReport_returnsCorrectCategories() {
    givenTripExists();
    CategoryTotal food = categoryTotal("food", "320.00");
    CategoryTotal transport = categoryTotal("transport", "150.50");
    when(expenseRepository.findCategoryTotalsByTripId(TRIP_ID))
        .thenReturn(List.of(food, transport));

    CategoryReportResponse response = reportService.getCategoryReport(TRIP_ID);

    assertThat(response.tripId()).isEqualTo(TRIP_ID);
    assertThat(response.baseCurrency()).isEqualTo("PLN");
    assertThat(response.categories()).hasSize(2);
    assertThat(response.categories().get(0).category()).isEqualTo("food");
    assertThat(response.categories().get(0).total()).isEqualByComparingTo("320.00");
  }

  @Test
  void getCategoryReport_nullCategory() {
    givenTripExists();
    CategoryTotal nullCat = categoryTotal(null, "50.00");
    when(expenseRepository.findCategoryTotalsByTripId(TRIP_ID)).thenReturn(List.of(nullCat));

    CategoryReportResponse response = reportService.getCategoryReport(TRIP_ID);

    assertThat(response.categories().get(0).category()).isEqualTo("");
  }

  @Test
  void getCategoryReport_tripNotFound_throwsResourceNotFoundException() {
    when(tripRepository.findById(TRIP_ID)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> reportService.getCategoryReport(TRIP_ID))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  // ==== getSettlementReport

  @Test
  void getSettlementReport_simpleDebt_returnsCorrectSettlement() {
    givenTripExists();
    DebtEntry simple = debtEntry(USER_A, "alice", USER_B, "bob", "100.00");
    when(expenseSplitRepository.findGrossDebtsByTripId(TRIP_ID)).thenReturn(List.of(simple));

    SettlementReportResponse response = reportService.getSettlementReport(TRIP_ID);

    assertThat(response.settlements()).hasSize(1);
    assertThat(response.settlements().get(0).debtorId()).isEqualTo(USER_A);
    assertThat(response.settlements().get(0).creditorId()).isEqualTo(USER_B);
    assertThat(response.settlements().get(0).amount()).isEqualByComparingTo("100.00");
  }

  @Test
  void getSettlementReport_netting_returnsNetAmount() {
    givenTripExists();
    // A owes B 150, B owes A 50, net: A owes B 100
    DebtEntry aOwesB = debtEntry(USER_A, "alice", USER_B, "bob", "150.00");
    DebtEntry bOwesA = debtEntry(USER_B, "bob", USER_A, "alice", "50.00");
    when(expenseSplitRepository.findGrossDebtsByTripId(TRIP_ID))
        .thenReturn(List.of(aOwesB, bOwesA));

    SettlementReportResponse response = reportService.getSettlementReport(TRIP_ID);

    assertThat(response.settlements()).hasSize(1);
    assertThat(response.settlements().get(0).debtorId()).isEqualTo(USER_A);
    assertThat(response.settlements().get(0).creditorId()).isEqualTo(USER_B);
    assertThat(response.settlements().get(0).amount()).isEqualByComparingTo("100.00");
  }

  @Test
  void getSettlementReport_equalDebts_returnsNoSettlements() {
    givenTripExists();
    // A owes B 100, B owes A 100, net: 0, nothing to settle
    DebtEntry aOwesB = debtEntry(USER_A, "alice", USER_B, "bob", "100.00");
    DebtEntry bOwesA = debtEntry(USER_B, "bob", USER_A, "alice", "100.00");
    when(expenseSplitRepository.findGrossDebtsByTripId(TRIP_ID))
        .thenReturn(List.of(aOwesB, bOwesA));

    SettlementReportResponse response = reportService.getSettlementReport(TRIP_ID);

    assertThat(response.settlements()).isEmpty();
  }

  @Test
  void getSettlementReport_noExpenses_returnsEmptySettlements() {
    givenTripExists();
    when(expenseSplitRepository.findGrossDebtsByTripId(TRIP_ID)).thenReturn(List.of());

    SettlementReportResponse response = reportService.getSettlementReport(TRIP_ID);

    assertThat(response.settlements()).isEmpty();
  }

  @Test
  void getSettlementReport_tripNotFound_throwsResourceNotFoundException() {
    when(tripRepository.findById(TRIP_ID)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> reportService.getSettlementReport(TRIP_ID))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  // ==== helpers

  private void givenTripExists() {
    Trip trip = mock(Trip.class);
    when(trip.getBaseCurrency()).thenReturn("PLN");
    when(tripRepository.findById(TRIP_ID)).thenReturn(Optional.of(trip));
  }

  private CategoryTotal categoryTotal(String category, String total) {
    CategoryTotal mock = mock(CategoryTotal.class);
    when(mock.getCategory()).thenReturn(category);
    when(mock.getTotal()).thenReturn(new BigDecimal(total));
    return mock;
  }

  private DebtEntry debtEntry(
      UUID debtorId,
      String debtorUsername,
      UUID creditorId,
      String creditorUsername,
      String amount) {
    DebtEntry mock = mock(DebtEntry.class);
    when(mock.getDebtorId()).thenReturn(debtorId);
    when(mock.getDebtorUsername()).thenReturn(debtorUsername);
    when(mock.getCreditorId()).thenReturn(creditorId);
    when(mock.getCreditorUsername()).thenReturn(creditorUsername);
    when(mock.getTotalAmount()).thenReturn(new BigDecimal(amount));
    return mock;
  }
}
