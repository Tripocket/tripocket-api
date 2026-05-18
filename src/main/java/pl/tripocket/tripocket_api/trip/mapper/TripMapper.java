package pl.tripocket.tripocket_api.trip.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.tripocket.tripocket_api.trip.dto.ParticipantResponse;
import pl.tripocket.tripocket_api.trip.dto.TripResponse;
import pl.tripocket.tripocket_api.trip.model.Trip;
import pl.tripocket.tripocket_api.trip.model.TripParticipant;

@Mapper(componentModel = "spring")
public interface TripMapper {

    @Mapping(target = "parentTripId", source = "parentTrip.id")
    @Mapping(target = "subTrips", source = "subTrips")
    TripResponse toResponse(Trip trip);

    @Mapping(target = "id", source = "user.id")
    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "displayName", source = "user.displayName")
    @Mapping(target = "role", expression = "java(participant.getRole().name())")
    @Mapping(target = "email", source = "user.email")
    ParticipantResponse toParticipantResponse(TripParticipant participant);
}