package pl.tripocket.tripocket_api.trip.report.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import pl.tripocket.tripocket_api.trip.report.dto.CategoryReportResponse;
import pl.tripocket.tripocket_api.trip.report.dto.CategoryReportResponse.CategoryEntry;
import pl.tripocket.tripocket_api.trip.report.dto.SettlementReportResponse;
import pl.tripocket.tripocket_api.trip.report.dto.SettlementReportResponse.Settlement;

// FIXME replace hardcoded response with DB query once it's ready
@Service
public class ReportService {

  private static final UUID USER_A = UUID.fromString("00000000-0000-0000-0000-000000000010");
  private static final UUID USER_B = UUID.fromString("00000000-0000-0000-0000-000000000011");

  public CategoryReportResponse getCategoryReport(UUID tripId) {
    return new CategoryReportResponse(
        tripId,
        "PLN",
        List.of(
            new CategoryEntry("food", new BigDecimal("320.00")),
            new CategoryEntry("transport", new BigDecimal("150.50")),
            new CategoryEntry("accommodation", new BigDecimal("800.00"))));
  }

  public SettlementReportResponse getSettlementReport(UUID tripId) {
    return new SettlementReportResponse(
        tripId,
        "PLN",
        List.of(
            new Settlement(USER_A, "janek88", USER_B, "tomasz_w", new BigDecimal("215.50"))));
  }
}
