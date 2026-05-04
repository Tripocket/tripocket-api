package pl.tripocket.tripocket_api.trip.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import pl.tripocket.tripocket_api.trip.dto.*;
import pl.tripocket.tripocket_api.trip.mapper.TripMapper;
import pl.tripocket.tripocket_api.trip.model.Trip;
import pl.tripocket.tripocket_api.trip.service.TripService;

import java.util.UUID;

@RestController
@RequestMapping("/api/trips")
@RequiredArgsConstructor
public class TripController {

    private final TripService tripService;
    private final TripMapper tripMapper;

    @PostMapping
    public ResponseEntity<TripResponse> createTrip(
            @RequestBody @Valid TripCreateRequest request,
            JwtAuthenticationToken token) {

        UUID userId = UUID.fromString(token.getName());
        Trip trip = tripService.createTrip(request, userId);
        return ResponseEntity.ok(tripMapper.toResponse(trip));
    }

    @PostMapping("/{tripId}/invite")
    public ResponseEntity<Void> inviteUser(
            @PathVariable UUID tripId,
            @RequestBody InviteRequest request,
            JwtAuthenticationToken token) {

        UUID requesterId = UUID.fromString(token.getName());
        tripService.inviteUser(tripId, request, requesterId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{tripId}")
    public ResponseEntity<TripResponse> updateTrip(
            @PathVariable UUID tripId,
            @RequestBody TripUpdateRequest request,
            JwtAuthenticationToken token) {

        UUID requesterId = UUID.fromString(token.getName());
        Trip updatedTrip = tripService.updateTrip(tripId, request, requesterId);
        return ResponseEntity.ok(tripMapper.toResponse(updatedTrip));
    }

    @DeleteMapping("/{tripId}/participants/{userId}")
    public ResponseEntity<Void> removeParticipant(
            @PathVariable UUID tripId,
            @PathVariable UUID userId,
            JwtAuthenticationToken token) {

        UUID requesterId = UUID.fromString(token.getName());
        tripService.removeParticipant(tripId, userId, requesterId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{tripId}/participants/{userId}/role")
    public ResponseEntity<Void> changeParticipantRole(
            @PathVariable UUID tripId,
            @PathVariable UUID userId,
            @RequestBody @Valid ChangeRoleRequest request,
            JwtAuthenticationToken token) {

        UUID requesterId = UUID.fromString(token.getName());
        tripService.changeParticipantRole(tripId, userId, request, requesterId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{tripId}")
    public ResponseEntity<Void> deleteTrip(
            @PathVariable UUID tripId,
            JwtAuthenticationToken token) {

        UUID requesterId = UUID.fromString(token.getName());
        tripService.deleteTrip(tripId, requesterId);
        return ResponseEntity.noContent().build();
    }
}