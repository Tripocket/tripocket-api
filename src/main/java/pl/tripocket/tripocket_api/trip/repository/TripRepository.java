package pl.tripocket.tripocket_api.trip.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.tripocket.tripocket_api.trip.model.Trip;
import java.util.UUID;

public interface TripRepository extends JpaRepository<Trip, UUID> {
}