package pl.tripocket.tripocket_api.trip.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import pl.tripocket.tripocket_api.trip.dto.*;
import pl.tripocket.tripocket_api.trip.service.InvitationService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class InvitationController {

    private final InvitationService invitationService;

    /**
     * Punkt 6.1: Przeniesione z TripController.
     * Wysyła zaproszenie do użytkownika w ramach konkretnej podróży.
     */
    @PostMapping("/trips/{tripId}/invitations")
    public ResponseEntity<InvitationResponse> invite(
            @PathVariable UUID tripId,
            @RequestBody InvitationRequest request,
            JwtAuthenticationToken token) {

        UUID senderId = UUID.fromString(token.getName());
        return ResponseEntity.ok(invitationService.sendInvitation(tripId, request, senderId));
    }

    /**
     * Pobiera listę oczekujących zaproszeń dla zalogowanego użytkownika.
     */
    @GetMapping("/invitations")
    public ResponseEntity<List<InvitationDTO>> getMyInvitations(JwtAuthenticationToken token) {
        UUID userId = UUID.fromString(token.getName());
        return ResponseEntity.ok(invitationService.getPendingInvitations(userId));
    }

    /**
     * Akceptacja lub odrzucenie zaproszenia.
     */
    @PutMapping("/invitations/{id}/respond")
    public ResponseEntity<Void> respond(
            @PathVariable UUID id,
            @RequestBody InvitationRespondRequest request) {
        invitationService.respond(id, request);
        return ResponseEntity.ok().build();
    }
}