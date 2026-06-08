package pl.tripocket.tripocket_api.trip.dto;

import java.util.UUID;

public record TripStatusResponse(
        UUID id,
        String status,
        String message
) {}