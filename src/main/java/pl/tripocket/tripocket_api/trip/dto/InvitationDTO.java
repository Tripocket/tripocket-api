package pl.tripocket.tripocket_api.trip.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

// Używane w GET /api/invitations
public record InvitationDTO(
        UUID invitationId,
        UUID tripId,
        String tripName,
        String inviterUsername,
        String role
) {}
