package pl.tripocket.tripocket_api.trip.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record TripResponse(
        UUID id,
        String country,
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal budget,
        String currencyPrimary,
        String currencySecondary,
        List<ParticipantResponse> participants
) {}