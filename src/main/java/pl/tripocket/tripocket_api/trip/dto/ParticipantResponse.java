package pl.tripocket.tripocket_api.trip.dto;

public record ParticipantResponse(
        String username,
        String displayName,
        String role
) {}