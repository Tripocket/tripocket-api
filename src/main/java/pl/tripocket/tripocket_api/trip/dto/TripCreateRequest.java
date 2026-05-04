package pl.tripocket.tripocket_api.trip.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public record TripCreateRequest(
        @NotBlank String country,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate,
        @Positive BigDecimal budget,
        @Size(min = 3, max = 3) String currencyPrimary,
        @Size(min = 3, max = 3) String currencySecondary
) {}