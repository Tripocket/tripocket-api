package pl.tripocket.tripocket_api.trip.service;

import lombok.RequiredArgsConstructor;
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

        Invitation invitation = Invitation.builder()
                .trip(trip)
                .inviter(inviter)
                .invitee(invitee)
                .status(InvitationStatus.PENDING)
                .build();

        invitationRepository.save(invitation);
        return new InvitationResponse("Zaproszenie wysłane do " + request.username());
    }

    public List<InvitationDTO> getPendingInvitations(UUID userId) {
        return invitationRepository.findAllByInviteeIdAndStatus(userId, InvitationStatus.PENDING).stream()
                .map(inv -> new InvitationDTO(
                        inv.getId(),
                        inv.getTrip().getName(),
                        inv.getInviter().getUsername(),
                        "PARTICIPANT" // Domyślna rola przy zaproszeniu
                ))
                .toList();
    }

    @Transactional
    public void respond(UUID invitationId, InvitationRespondRequest request) {
        Invitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new ResourceNotFoundException("Zaproszenie nie istnieje"));

        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new IllegalStateException("To zaproszenie zostało już obsłużone.");
        }

        if (request.accept()) {
            invitation.setStatus(InvitationStatus.ACCEPTED);
            // Dodaj użytkownika jako uczestnika
            TripParticipant participant = TripParticipant.builder()
                    .trip(invitation.getTrip())
                    .user(invitation.getInvitee())
                    .role(TripRole.PARTICIPANT)
                    .build();

            invitation.getTrip().getParticipants().add(participant);
            tripRepository.save(invitation.getTrip());
        } else {
            invitation.setStatus(InvitationStatus.REJECTED);
        }

        invitationRepository.save(invitation);
    }
}