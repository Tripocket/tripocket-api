package pl.tripocket.tripocket_api.trip.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.tripocket.tripocket_api.trip.model.Invitation;
import pl.tripocket.tripocket_api.trip.model.InvitationStatus;

import java.util.List;
import java.util.UUID;

@Repository
public interface InvitationRepository extends JpaRepository<Invitation, UUID> {
    // Dla GET /api/invitations
    List<Invitation> findAllByInviteeIdAndStatus(UUID inviteeId, InvitationStatus status);

    // Zapobiega duplikatom oczekujących zaproszeń
    boolean existsByTripIdAndInviteeIdAndStatus(UUID tripId, UUID inviteeId, InvitationStatus status);
}