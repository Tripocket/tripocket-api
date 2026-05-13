package pl.tripocket.tripocket_api.trip.dto;

import java.util.UUID;

public record ParticipantResponse(
        UUID id,
        String username,
        String displayName,
        String role
) {}