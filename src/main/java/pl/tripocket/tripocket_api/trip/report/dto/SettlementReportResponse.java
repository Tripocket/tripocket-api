package pl.tripocket.tripocket_api.trip.report.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record SettlementReportResponse(
    UUID tripId,
    String baseCurrency,
    List<Settlement> settlements) {

  @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
  public record Settlement(
      UUID debtorId,
      String debtorUsername,
      UUID creditorId,
      String creditorUsername,
      BigDecimal amount) {}
}
