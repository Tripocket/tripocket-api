package pl.tripocket.tripocket_api.trip.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.tripocket.tripocket_api.trip.model.Trip;

import java.util.List;
import java.util.UUID;

@Repository
public interface TripRepository extends JpaRepository<Trip, UUID> {

    /**
     * Znajduje wszystkie główne podróże (bez parent_trip_id),
     * w których dany użytkownik bierze udział jako uczestnik.
     */
    List<Trip> findAllByParticipantsUserIdAndParentTripIsNull(UUID userId);
}