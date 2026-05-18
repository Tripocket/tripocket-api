package pl.tripocket.tripocket_api.trip.dto;

// Używane w PUT /api/invitations/{id}/respond
public record InvitationRespondRequest(
        boolean accept // true = ACCEPTED, false = REJECTED
) {}