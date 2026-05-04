package pl.tripocket.tripocket_api.trip.dto;

import jakarta.validation.constraints.NotBlank;

public record ChangeRoleRequest(
        @NotBlank String newRole
) {}
