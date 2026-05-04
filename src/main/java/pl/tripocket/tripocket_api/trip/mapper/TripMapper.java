package pl.tripocket.tripocket_api.trip.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.tripocket.tripocket_api.trip.dto.ParticipantResponse;
import pl.tripocket.tripocket_api.trip.dto.TripResponse;
import pl.tripocket.tripocket_api.trip.model.Trip;
import pl.tripocket.tripocket_api.trip.model.TripParticipant;

@Mapper(componentModel = "spring")
public interface TripMapper {

    TripResponse toResponse(Trip trip);

    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "displayName", source = "user.displayName")
    ParticipantResponse toParticipantResponse(TripParticipant participant);
}