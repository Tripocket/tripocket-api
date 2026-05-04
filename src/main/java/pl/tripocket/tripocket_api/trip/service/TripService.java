package pl.tripocket.tripocket_api.trip.service;

import pl.tripocket.tripocket_api.trip.dto.ChangeRoleRequest;
import pl.tripocket.tripocket_api.trip.dto.InviteRequest;
import pl.tripocket.tripocket_api.trip.dto.TripCreateRequest;
import pl.tripocket.tripocket_api.trip.dto.TripUpdateRequest;
import pl.tripocket.tripocket_api.trip.model.Trip;
import java.util.UUID;

public interface TripService {
    Trip createTrip(TripCreateRequest request, UUID creatorId);
    Trip updateTrip(UUID tripId, TripUpdateRequest request, UUID requesterId);
    void inviteUser(UUID tripId, InviteRequest request, UUID requesterId);
    void removeParticipant(UUID tripId, UUID userIdToRemove, UUID requesterId);
    void changeParticipantRole(UUID tripId, UUID targetUserId, ChangeRoleRequest request, UUID requesterId);
    void deleteTrip(UUID tripId, UUID requesterId);
}