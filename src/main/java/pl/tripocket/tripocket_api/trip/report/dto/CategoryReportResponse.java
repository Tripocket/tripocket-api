package pl.tripocket.tripocket_api.trip.report.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public record CategoryReportResponse(
    UUID tripId,
    String baseCurrency,
    List<CategoryEntry> categories) {

  @JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
  public record CategoryEntry(String category, BigDecimal total) {}
}
