package pl.tripocket.tripocket_api.trip.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record TripCreateRequest(
        @JsonProperty("parent_trip_id") UUID parentTripId,
        @NotBlank @JsonProperty("name") String name,
        @NotBlank @JsonProperty("country") String country,
        @NotNull @JsonProperty("start_date") LocalDate startDate,
        @NotNull @JsonProperty("end_date") LocalDate endDate,
        @NotNull @Positive @JsonProperty("budget") BigDecimal budget,
        @NotBlank @JsonProperty("base_currency") String baseCurrency,
        @JsonProperty("transport_mode") String transportMode,
        @JsonProperty("trip_type") String tripType
) {}