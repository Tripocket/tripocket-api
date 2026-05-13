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
        @JsonProperty("parent_trip_id") UUID parentTripId,
        String country,
        @JsonProperty("start_date") LocalDate startDate,
        @JsonProperty("end_date") LocalDate endDate,
        BigDecimal budget,
        @JsonProperty("base_currency") String baseCurrency,
        @JsonProperty("transport_mode") String transportMode,
        @JsonProperty("trip_type") String tripType,
        TripStatus status,
        List<ParticipantResponse> participants,
        @JsonProperty("sub_trips") List<TripResponse> subTrips
) {}