package pl.tripocket.tripocket_api.trip.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TripUpdateRequest(
        String country,
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal budget,
        String currencyPrimary,
        String currencySecondary
) {}
