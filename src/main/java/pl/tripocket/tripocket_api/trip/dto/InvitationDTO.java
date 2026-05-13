package pl.tripocket.tripocket_api.trip.dto;

import java.util.UUID;

// Używane w GET /api/invitations
public record InvitationDTO(
        UUID id,
        String tripName,
        String inviterUsername,
        String role
) {}
