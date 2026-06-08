package pl.tripocket.tripocket_api.trip.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record TripCreateRequest(
        @JsonProperty("parentTripId") UUID parentTripId,
        @NotBlank @Size(min=4, max=255) @JsonProperty("name") String name,
        @NotBlank @Size(min=4, max=127) @JsonProperty("country") String country,
        @NotNull @JsonProperty("startDate") LocalDate startDate,
        @NotNull @JsonProperty("endDate") LocalDate endDate,
        @NotNull @Positive @JsonProperty("budget") BigDecimal budget,
        @NotBlank @Size(min=3, max=3) @JsonProperty("baseCurrency") String baseCurrency,
        @JsonProperty("transportMode") String transportMode,
        @JsonProperty("tripType") String tripType
) {}