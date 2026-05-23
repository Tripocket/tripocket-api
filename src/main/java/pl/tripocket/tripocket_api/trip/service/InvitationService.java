package pl.tripocket.tripocket_api.trip.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.tripocket.tripocket_api.auth.user.model.User;
import pl.tripocket.tripocket_api.auth.user.repository.UserRepository;
import pl.tripocket.tripocket_api.common.exception.ResourceNotFoundException;
import pl.tripocket.tripocket_api.trip.dto.*;
import pl.tripocket.tripocket_api.trip.model.*;
import pl.tripocket.tripocket_api.trip.repository.InvitationRepository;
import pl.tripocket.tripocket_api.trip.repository.TripRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InvitationService {

    private final InvitationRepository invitationRepository;
    private final TripRepository tripRepository;
    private final UserRepository userRepository;

    @Transactional
    public InvitationResponse sendInvitation(UUID tripId, InvitationRequest request, UUID senderId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Podróż nie istnieje"));

        User inviter = userRepository.findById(senderId)
                .orElseThrow(() -> new ResourceNotFoundException("Zapraszający nie istnieje"));

        boolean isOwner = trip.getParticipants().stream()
                .anyMatch(p -> p.getUser().getId().equals(senderId) && p.getRole() == TripRole.OWNER);

        if (!isOwner) {
            throw new org.springframework.security.access.AccessDeniedException("Tylko właściciel może zapraszać do podróży.");
        }

        User invitee = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new ResourceNotFoundException("Użytkownik " + request.username() + " nie istnieje"));

        // Sprawdź czy już jest w podróży
        boolean isParticipant = trip.getParticipants().stream()
                .anyMatch(p -> p.getUser().getId().equals(invitee.getId()));
        if (isParticipant) throw new IllegalStateException("Użytkownik już jest uczestnikiem.");

        // Sprawdź czy zaproszenie już wisi
        if (invitationRepository.existsByTripIdAndInviteeIdAndStatus(tripId, invitee.getId(), InvitationStatus.PENDING)) {
            throw new IllegalStateException("Zaproszenie jest już w toku.");
        }

        // Sprawdza role do przypisania
        TripRole intendedRole;
        try {
            intendedRole = request.role();
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Niepoprawna rola: " + request.role() + ". Dozwolone wartości: OWNER, PARTICIPANT");
        }


        Invitation invitation = Invitation.builder()
                .trip(trip)
                .inviter(inviter)
                .invitee(invitee)
                .role(intendedRole)
                .status(InvitationStatus.PENDING)
                .build();

        invitationRepository.save(invitation);
        return new InvitationResponse("Zaproszenie wysłane do " + request.username());
    }

    public List<InvitationDTO> getPendingInvitations(JwtAuthenticationToken token) {
        // Pobiera tylko zaproszenia, gdzie invitee_id == currentUserId
        UUID currentUserId = UUID.fromString(token.getName());
        return invitationRepository.findAllByInviteeIdAndStatus(currentUserId, InvitationStatus.PENDING)
                .stream()
                .map(inv -> new InvitationDTO(
                        inv.getId(),
                        inv.getTrip().getId(),
                        inv.getTrip().getName(),
                        inv.getInviter().getUsername(),
                        inv.getRole().name()
                ))
                .toList();
    }

    @Transactional
    public void respond(UUID invitationId, InvitationRespondRequest request, JwtAuthenticationToken token) {
        UUID currentUserId = UUID.fromString(token.getName());
        Invitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new ResourceNotFoundException("Zaproszenie nie istnieje"));

        if (!invitation.getInvitee().getId().equals(currentUserId)) {
            throw new org.springframework.security.access.AccessDeniedException("Nie możesz zaakceptować cudzego zaproszenia.");
        }

        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new IllegalStateException("To zaproszenie zostało już obsłużone.");
        }

        if (request.accept()) {
            invitation.setStatus(InvitationStatus.ACCEPTED);
            // Dodaj użytkownika jako uczestnika
            TripParticipant participant = TripParticipant.builder()
                    .trip(invitation.getTrip())
                    .user(invitation.getInvitee())
                    .role(invitation.getRole())
                    .build();

            invitation.getTrip().getParticipants().add(participant);
            tripRepository.save(invitation.getTrip());
        } else {
            invitation.setStatus(InvitationStatus.REJECTED);
        }

        invitationRepository.save(invitation);
    }


    //  --- HELPERY ---

    private Trip getTripIfParticipant(UUID tripId, JwtAuthenticationToken token) {
        UUID requesterId = UUID.fromString(token.getName());
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Podróż nie istnieje"));

        // Sprawdzenie, czy użytkownik ma rolę PARTICIPANT w tej wycieczce
        boolean isMember = trip.getParticipants().stream()
                .anyMatch(p -> p.getUser().getId().equals(requesterId) &&
                        (p.getRole() == TripRole.PARTICIPANT || p.getRole() == TripRole.OWNER));

        if (!isMember) {
            throw new AccessDeniedException("Brak uprawnień: Tylko Uczestnik może wykonać tę akcję.");
        }
        return trip;
    }
}