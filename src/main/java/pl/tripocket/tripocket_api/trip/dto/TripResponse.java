package pl.tripocket.tripocket_api.trip.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import pl.tripocket.tripocket_api.trip.model.TripStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record TripResponse(
        UUID id,
        String name,
        @JsonProperty("parentTripId") UUID parentTripId,
        String country,
        @JsonProperty("startDate") LocalDate startDate,
        @JsonProperty("endDate") LocalDate endDate,
        BigDecimal budget,
        @JsonProperty("baseCurrency") String baseCurrency,
        @JsonProperty("transportMode") String transportMode,
        @JsonProperty("tripType") String tripType,
        TripStatus status,
        List<ParticipantResponse> participants,
        @JsonProperty("subTrips") List<TripResponse> subTrips
) {}