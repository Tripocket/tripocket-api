package pl.tripocket.tripocket_api.trip.controller;

import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import pl.tripocket.tripocket_api.trip.dto.*;
import pl.tripocket.tripocket_api.trip.service.InvitationService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("")
@RequiredArgsConstructor
public class InvitationController {

    private final InvitationService invitationService;

    /**
     * Wysyła zaproszenie do użytkownika w ramach konkretnej podróży.
     */
    @PostMapping("/trips/{tripId}/invitations")
    public ResponseEntity<InvitationResponse> invite(
            @PathVariable UUID tripId,
            @Valid @RequestBody InvitationRequest request,
            JwtAuthenticationToken token) {

        UUID senderId = UUID.fromString(token.getName());
        return ResponseEntity.ok(invitationService.sendInvitation(tripId, request, senderId));
    }

    /**
     * Pobiera listę oczekujących zaproszeń dla zalogowanego użytkownika.
     */
    @GetMapping("/invitations")
    public ResponseEntity<List<InvitationDTO>> getMyPendingInvitations(JwtAuthenticationToken token) {
        List<InvitationDTO> invitations = invitationService.getPendingInvitations(token);
        return ResponseEntity.ok(invitations);
    }

    /**
     * Akceptacja lub odrzucenie zaproszenia.
     */
    @PutMapping("/invitations/{id}/respond")
    public ResponseEntity<Void> respond(
            @PathVariable UUID id,
            @Valid @RequestBody InvitationRespondRequest request, JwtAuthenticationToken token) {
        invitationService.respond(id, request, token);
        return ResponseEntity.ok().build();
    }
}