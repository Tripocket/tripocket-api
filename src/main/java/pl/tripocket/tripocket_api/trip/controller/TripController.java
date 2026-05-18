package pl.tripocket.tripocket_api.trip.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import pl.tripocket.tripocket_api.trip.dto.*;
import pl.tripocket.tripocket_api.trip.service.TripService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/trips")
@RequiredArgsConstructor
public class TripController {

    private final TripService tripService;

    // --- ZARZĄDZANIE PODRÓŻAMI ---

    @GetMapping
    public ResponseEntity<List<TripResponse>> getMainTrips(JwtAuthenticationToken token) {
        return ResponseEntity.ok(tripService.getUserMainTrips(token));
    }

    @PostMapping
    public ResponseEntity<TripStatusResponse> createTrip(
            @RequestBody @Valid TripCreateRequest request,
            JwtAuthenticationToken token) {
        return new ResponseEntity<>(tripService.createTrip(request, token), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TripResponse> getTripDetails(@PathVariable UUID id) {
        return ResponseEntity.ok(tripService.getTripDetails(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TripResponse> updateTrip(
            @PathVariable UUID id,
            @RequestBody @Valid TripUpdateRequest request,
            JwtAuthenticationToken token) {
        return ResponseEntity.ok(tripService.updateTrip(id, request, token));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTrip(@PathVariable UUID id, JwtAuthenticationToken token) {
        tripService.deleteTrip(id, token);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{tripId}/participants")
    public ResponseEntity<List<ParticipantResponse>> getParticipants(@PathVariable UUID tripId) {
        return ResponseEntity.ok(tripService.getParticipants(tripId));
    }

    @PutMapping("/{tripId}/participants/{userId}")
    public ResponseEntity<Void> changeParticipantRole(
            @PathVariable UUID tripId,
            @PathVariable UUID userId,
            @RequestBody @Valid ChangeRoleRequest request,
            JwtAuthenticationToken token) {
        tripService.changeParticipantRole(tripId, userId, request, token);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{tripId}/participants/{userId}")
    public ResponseEntity<Void> removeParticipant(
            @PathVariable UUID tripId,
            @PathVariable UUID userId,
            JwtAuthenticationToken token) {
        tripService.removeParticipant(tripId, userId, token);
        return ResponseEntity.noContent().build();
    }
}