package pl.tripocket.tripocket_api.trip.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.tripocket.tripocket_api.auth.user.model.User;
import pl.tripocket.tripocket_api.auth.user.repository.UserRepository;
import pl.tripocket.tripocket_api.common.exception.ResourceNotFoundException;
import pl.tripocket.tripocket_api.trip.dto.ChangeRoleRequest;
import pl.tripocket.tripocket_api.trip.dto.InviteRequest;
import pl.tripocket.tripocket_api.trip.dto.TripCreateRequest;
import pl.tripocket.tripocket_api.trip.dto.TripUpdateRequest;
import pl.tripocket.tripocket_api.trip.model.*;
import pl.tripocket.tripocket_api.trip.repository.TripRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TripServiceImpl implements TripService {

    private final TripRepository tripRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public Trip createTrip(TripCreateRequest request, UUID creatorId) {
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        //tworzy obiekt podróży
        Trip trip = new Trip();
        trip.setCountry(request.country());
        trip.setStartDate(request.startDate());
        trip.setEndDate(request.endDate());
        trip.setBudget(request.budget());
        trip.setCurrencyPrimary(request.currencyPrimary());
        trip.setCurrencySecondary(request.currencySecondary());

        //automatycznie dodaje twórcę jako OWNERA (WF-08)
        TripParticipant owner = TripParticipant.builder()
                .id(new TripParticipantId(null, creatorId)) //ID podróży uzupełni się po save
                .trip(trip)
                .user(creator)
                .role("OWNER")
                .build();

        trip.getParticipants().add(owner);

        return tripRepository.save(trip);
    }

    @Override
    @Transactional
    public void inviteUser(UUID tripId, InviteRequest request, UUID requesterId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found"));

        //sprawdza, czy osoba zapraszająca jest OWNER'em tej podróży
        boolean isOwner = trip.getParticipants().stream()
                .anyMatch(p -> p.getUser().getId().equals(requesterId) && "OWNER".equals(p.getRole()));

        if (!isOwner) {
            throw new RuntimeException("Only owner can invite others");
        }

        User userToInvite = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        TripParticipant participant = TripParticipant.builder()
                .id(new TripParticipantId(tripId, userToInvite.getId()))
                .trip(trip)
                .user(userToInvite)
                .role(request.role())
                .build();

        trip.getParticipants().add(participant);
        tripRepository.save(trip);
    }

    private void checkIfOwner(Trip trip, UUID userId) {
        boolean isOwner = trip.getParticipants().stream()
                .anyMatch(p -> p.getUser().getId().equals(userId) && "OWNER".equals(p.getRole()));
        if (!isOwner) {
            throw new RuntimeException("Access denied: Only owner can perform this action");
        }
    }

    @Override
    @Transactional
    public Trip updateTrip(UUID tripId, TripUpdateRequest request, UUID requesterId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found"));

        // Tylko OWNER może edytować detale podróży
        checkIfOwner(trip, requesterId);

        if (request.country() != null) trip.setCountry(request.country());
        if (request.startDate() != null) trip.setStartDate(request.startDate());
        if (request.endDate() != null) trip.setEndDate(request.endDate());
        if (request.budget() != null) trip.setBudget(request.budget());
        if (request.currencyPrimary() != null) trip.setCurrencyPrimary(request.currencyPrimary());
        if (request.currencySecondary() != null) trip.setCurrencySecondary(request.currencySecondary());

        return tripRepository.save(trip);
    }

    @Override
    @Transactional
    public void removeParticipant(UUID tripId, UUID userIdToRemove, UUID requesterId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found"));

        checkIfOwner(trip, requesterId);

        if (userIdToRemove.equals(requesterId)) {
            throw new RuntimeException("You cannot remove yourself from the trip");
        }

        trip.getParticipants().removeIf(p -> p.getUser().getId().equals(userIdToRemove));
        tripRepository.save(trip);
    }

    @Override
    @Transactional
    public void changeParticipantRole(UUID tripId, UUID targetUserId, ChangeRoleRequest request, UUID requesterId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found"));

        checkIfOwner(trip, requesterId);

        //znajduje uczestnika w kolekcji wycieczki
        TripParticipant participantToChange = trip.getParticipants().stream()
                .filter(p -> p.getUser().getId().equals(targetUserId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Participant not found in this trip"));

        //nie pozwól zmienić roli samemu sobie
        if (targetUserId.equals(requesterId) && !"OWNER".equals(request.newRole())) {
            throw new RuntimeException("You cannot demote yourself from OWNER role");
        }

        //aktualizacja
        participantToChange.setRole(request.newRole());

        tripRepository.save(trip);
    }

    @Override
    @Transactional
    public void deleteTrip(UUID tripId, UUID requesterId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found"));

        //tylko OWNER może usunąć
        checkIfOwner(trip, requesterId);

        //ON DELETE CASCADE w postgre = wyczyszczą sie odpowiednie rekordy z tabeli incydencji
        tripRepository.delete(trip);
    }
}