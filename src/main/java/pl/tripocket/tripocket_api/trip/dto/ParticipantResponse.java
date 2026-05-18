package pl.tripocket.tripocket_api.trip.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record ParticipantResponse(
        UUID id,
        String username,
        String displayName,
        String role,
        String email
) {}