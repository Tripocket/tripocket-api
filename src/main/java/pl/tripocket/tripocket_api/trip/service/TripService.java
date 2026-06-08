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
import pl.tripocket.tripocket_api.trip.mapper.TripMapper;
import pl.tripocket.tripocket_api.trip.model.*;
import pl.tripocket.tripocket_api.trip.repository.TripRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TripService {

    private final TripRepository tripRepository;
    private final UserRepository userRepository;
    private final TripMapper tripMapper;

    @Transactional
    public TripStatusResponse createTrip(TripCreateRequest request, JwtAuthenticationToken token) {
        UUID creatorId = UUID.fromString(token.getName());
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new ResourceNotFoundException("Użytkownik nie istnieje"));

        if (request.endDate().isBefore(request.startDate())) {
            throw new IllegalArgumentException("Data zakończenia nie może być wcześniejsza od rozpoczęcia");
        }

        Trip trip = new Trip();
        // 5.1: Pełne mapowanie pól z requestu
        trip.setName(request.name());
        trip.setCountry(request.country());
        trip.setStartDate(request.startDate());
        trip.setEndDate(request.endDate());
        trip.setBudget(request.budget());
        trip.setBaseCurrency(request.baseCurrency());
        trip.setTransportMode(request.transportMode());
        trip.setTripType(request.tripType());
        trip.setStatus(TripStatus.PLANNED); // Użycie Enuma zamiast Stringa

        /*
        if (trip.getId().equals(request.parentTripId())) {
            throw new IllegalArgumentException("Wycieczka nie może być swoją własną podwycieczką!");
        }
         */


        if (request.parentTripId() != null) {
            Trip parent = getTripIfOwner(request.parentTripId(), token);
            trip.setParentTrip(parent);
        }


        Trip saved = tripRepository.save(trip);

        TripParticipant owner = TripParticipant.builder()
                .trip(trip)
                .user(creator)
                .role(TripRole.OWNER)
                .build();

        saved.getParticipants().add(owner);

        tripRepository.save(saved);

        return new TripStatusResponse(saved.getId(), saved.getStatus().name(), "Podróż została utworzona pomyślnie.");
    }

    @Transactional
    public TripResponse updateTrip(UUID id, TripUpdateRequest request, JwtAuthenticationToken token) {
        Trip trip = getTripIfOwner(id, token);

        LocalDate start = request.startDate() == null ? trip.getStartDate() : request.startDate();
        LocalDate end = request.endDate() == null ? trip.getEndDate() : request.endDate();
        if (end.isBefore(start)) {
            throw new IllegalArgumentException("Data zakończenia nie może być wcześniejsza od rozpoczęcia");
        }

        // Mapowanie zmian z uwzględnieniem null-checków (opcjonalnie) lub nadpisywanie
        if (request.name() != null) trip.setName(request.name());
        if (request.country() != null) trip.setCountry(request.country());
        if (request.startDate() != null) trip.setStartDate(request.startDate());
        if (request.endDate() != null) trip.setEndDate(request.endDate());
        if (request.budget() != null) trip.setBudget(request.budget());
        if (request.baseCurrency() != null) trip.setBaseCurrency(request.baseCurrency());
        if (request.transportMode() != null) trip.setTransportMode(request.transportMode());
        if (request.tripType() != null) trip.setTripType(request.tripType());
        if (request.status() != null) trip.setStatus(request.status());

        return tripMapper.toResponse(tripRepository.save(trip));
    }

    @Transactional
    public void deleteTrip(UUID id, JwtAuthenticationToken token) {
        Trip trip = getTripIfOwner(id, token);
        tripRepository.delete(trip);
    }

    @Transactional
    public void changeParticipantRole(UUID tripId, UUID targetUserId, ChangeRoleRequest request, JwtAuthenticationToken token) {
        Trip trip = getTripIfOwner(tripId, token);

        TripParticipant participant = trip.getParticipants().stream()
                .filter(p -> p.getUser().getId().equals(targetUserId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Uczestnik nie znaleziony"));

        // Poprawione wywołanie request.role()
        try {
            participant.setRole(TripRole.valueOf(request.role().toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Niepoprawna rola: " + request.role());
        }

        tripRepository.save(trip);
    }

    @Transactional
    public void removeParticipant(UUID tripId, UUID userIdToRemove, JwtAuthenticationToken token) {
        Trip trip = getTripIfOwner(tripId, token);
        UUID requesterId = UUID.fromString(token.getName());

        if (userIdToRemove.equals(requesterId)) {
            throw new IllegalStateException("Nie możesz usunąć samego siebie z podróży.");
        }

        boolean removed = trip.getParticipants().removeIf(p -> p.getUser().getId().equals(userIdToRemove));
        if (!removed) {
            throw new ResourceNotFoundException("Użytkownik nie jest uczestnikiem tej podróży.");
        }

        tripRepository.save(trip);
    }

    public List<TripResponse> getUserMainTrips(JwtAuthenticationToken token) {
        UUID userId = UUID.fromString(token.getName());
        return tripRepository.findAllByParticipantsUserIdAndParentTripIsNull(userId).stream()
                .map(tripMapper::toResponse)
                .toList();
    }

    public TripResponse getTripDetails(UUID id) {
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Podróż o ID " + id + " nie istnieje"));
        return tripMapper.toResponse(trip);
    }

    public List<ParticipantResponse> getParticipants(UUID tripId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Podróż nie istnieje"));
        return trip.getParticipants().stream()
                .map(tripMapper::toParticipantResponse)
                .toList();
    }

    // --- HELPERY ---

    private Trip getTripIfOwner(UUID tripId, JwtAuthenticationToken token) {
        UUID requesterId = UUID.fromString(token.getName());
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Podróż nie istnieje"));

        // Poprawione porównanie roli na TripRole.OWNER
        boolean isOwner = trip.getParticipants().stream()
                .anyMatch(p -> p.getUser().getId().equals(requesterId) && p.getRole() == TripRole.OWNER);

        if (!isOwner) {
            // Użycie AccessDeniedException dla błędów uprawnień
            throw new AccessDeniedException("Brak uprawnień: Tylko Właściciel może wykonać tę akcję.");
        }
        return trip;
    }
}