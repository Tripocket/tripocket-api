package pl.tripocket.tripocket_api.trip.report.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.tripocket.tripocket_api.common.exception.ResourceNotFoundException;
import pl.tripocket.tripocket_api.trip.model.Trip;
import pl.tripocket.tripocket_api.trip.report.dto.CategoryReportResponse;
import pl.tripocket.tripocket_api.trip.report.dto.CategoryReportResponse.CategoryEntry;
import pl.tripocket.tripocket_api.trip.report.dto.SettlementReportResponse;
import pl.tripocket.tripocket_api.trip.report.dto.SettlementReportResponse.Settlement;
import pl.tripocket.tripocket_api.trip.repository.ExpenseRepository;
import pl.tripocket.tripocket_api.trip.repository.ExpenseSplitRepository;
import pl.tripocket.tripocket_api.trip.repository.ExpenseSplitRepository.DebtEntry;
import pl.tripocket.tripocket_api.trip.repository.TripRepository;

@Service
public class ReportService {

  private final TripRepository tripRepository;
  private final ExpenseRepository expenseRepository;
  private final ExpenseSplitRepository expenseSplitRepository;

  public ReportService(
      TripRepository tripRepository,
      ExpenseRepository expenseRepository,
      ExpenseSplitRepository expenseSplitRepository) {
    this.tripRepository = tripRepository;
    this.expenseRepository = expenseRepository;
    this.expenseSplitRepository = expenseSplitRepository;
  }

  @Transactional(readOnly = true)
  public CategoryReportResponse getCategoryReport(UUID tripId) {
    Trip trip =
        tripRepository
            .findById(tripId)
            .orElseThrow(() -> new ResourceNotFoundException("Trip not found: " + tripId));

    List<CategoryEntry> categories =
        expenseRepository.findCategoryTotalsByTripId(tripId).stream()
            .map(
                ct ->
                    new CategoryEntry(
                        ct.getCategory() != null ? ct.getCategory() : "", ct.getTotal()))
            .toList();

    return new CategoryReportResponse(tripId, trip.getBaseCurrency(), categories);
  }

  @Transactional(readOnly = true)
  public SettlementReportResponse getSettlementReport(UUID tripId) {
    Trip trip =
        tripRepository
            .findById(tripId)
            .orElseThrow(() -> new ResourceNotFoundException("Trip not found: " + tripId));

    List<DebtEntry> debts = expenseSplitRepository.findGrossDebtsByTripId(tripId);

    return new SettlementReportResponse(tripId, trip.getBaseCurrency(), netDebts(debts));
  }

  private List<Settlement> netDebts(List<DebtEntry> debts) {
    Map<String, BigDecimal> netAmounts = new LinkedHashMap<>();
    Map<String, UUID[]> pairs = new LinkedHashMap<>();
    Map<UUID, String> usernames = new HashMap<>();

    for (DebtEntry debt : debts) {
      usernames.put(debt.getDebtorId(), debt.getDebtorUsername());
      usernames.put(debt.getCreditorId(), debt.getCreditorUsername());

      boolean debtorIsFirst = debt.getDebtorId().compareTo(debt.getCreditorId()) < 0;
      UUID first = debtorIsFirst ? debt.getDebtorId() : debt.getCreditorId();
      UUID second = debtorIsFirst ? debt.getCreditorId() : debt.getDebtorId();
      BigDecimal delta = debtorIsFirst ? debt.getTotalAmount() : debt.getTotalAmount().negate();

      String key = first + ":" + second;
      pairs.putIfAbsent(key, new UUID[] {first, second});
      netAmounts.merge(key, delta, BigDecimal::add);
    }

    List<Settlement> settlements = new ArrayList<>();
    for (Map.Entry<String, BigDecimal> entry : netAmounts.entrySet()) {
      BigDecimal net = entry.getValue();
      if (net.compareTo(BigDecimal.ZERO) == 0) continue;

      UUID[] pair = pairs.get(entry.getKey());
      boolean firstOwesSecond = net.compareTo(BigDecimal.ZERO) > 0;
      UUID debtorId = firstOwesSecond ? pair[0] : pair[1];
      UUID creditorId = firstOwesSecond ? pair[1] : pair[0];
      BigDecimal amount = net.abs();

      settlements.add(
          new Settlement(
              debtorId, usernames.get(debtorId), creditorId, usernames.get(creditorId), amount));
    }

    return settlements;
  }
}
