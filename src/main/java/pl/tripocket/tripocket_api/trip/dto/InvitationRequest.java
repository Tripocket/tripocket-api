package pl.tripocket.tripocket_api.trip.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import pl.tripocket.tripocket_api.trip.model.TripRole;

public record InvitationRequest (
        @NotBlank @Size(min=4, max=32) String username,
        @NotNull TripRole role
        ) {}
