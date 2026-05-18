package pl.tripocket.tripocket_api.trip;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import pl.tripocket.tripocket_api.auth.user.model.User;
import pl.tripocket.tripocket_api.auth.user.repository.UserRepository;
import pl.tripocket.tripocket_api.trip.dto.TripCreateRequest;
import pl.tripocket.tripocket_api.trip.dto.TripStatusResponse;
import pl.tripocket.tripocket_api.trip.mapper.TripMapper;
import pl.tripocket.tripocket_api.trip.model.*;
import pl.tripocket.tripocket_api.trip.repository.TripRepository;
import pl.tripocket.tripocket_api.trip.service.TripService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TripServiceTest {

    @Mock private TripRepository tripRepository;
    @Mock private UserRepository userRepository;
    @Mock private TripMapper tripMapper;
    @Mock private JwtAuthenticationToken token;

    @InjectMocks private TripService tripService;

    private UUID userId;
    private User user;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        user = new User();
        user.setId(userId);

        // Mockowanie sub (ID użytkownika) z JWT
        when(token.getName()).thenReturn(userId.toString());
    }

    @Test
    void createTrip_Success() {
        // Given
        TripCreateRequest request = new TripCreateRequest(
                null, "Góry 2026", "Poland", LocalDate.now(),
                LocalDate.now().plusDays(5), new BigDecimal("2000.00"), "PLN", "CAR", "LEISURE"
                );

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(tripRepository.save(any(Trip.class))).thenAnswer(inv -> {
            Trip t = inv.getArgument(0);
            t.setId(UUID.randomUUID());
            return t;
        });

        // When
        TripStatusResponse response = tripService.createTrip(request, token);

        // Then
        assertNotNull(response.id());
        assertEquals("PLANNED", response.status());
        verify(tripRepository).save(any(Trip.class));
    }

    @Test
    void deleteTrip_Fails_WhenUserIsNotOwner() {
        // Given
        UUID tripId = UUID.randomUUID();
        Trip trip = new Trip();
        trip.setId(tripId);

        // Użytkownik jest uczestnikiem, ale nie właścicielem
        TripParticipant participant = TripParticipant.builder()
                .user(user)
                .role(TripRole.PARTICIPANT)
                .build();
        trip.setParticipants(new ArrayList<>(List.of(participant)));

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));

        // When & Then
        assertThrows(AccessDeniedException.class, () -> tripService.deleteTrip(tripId, token));
        verify(tripRepository, never()).delete(any());
    }

    @Test
    void removeParticipant_Success() {
        // Given
        UUID tripId = UUID.randomUUID();
        UUID toRemoveId = UUID.randomUUID();
        User toRemove = new User();
        toRemove.setId(toRemoveId);

        Trip trip = new Trip();
        trip.setParticipants(new ArrayList<>());

        // Dodaj właściciela (zalogowany user)
        trip.getParticipants().add(TripParticipant.builder().user(user).role(TripRole.OWNER).build());
        // Dodaj uczestnika do usunięcia
        trip.getParticipants().add(TripParticipant.builder().user(toRemove).role(TripRole.PARTICIPANT).build());

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));

        // When
        tripService.removeParticipant(tripId, toRemoveId, token);

        // Then
        assertEquals(1, trip.getParticipants().size());
        verify(tripRepository).save(trip);
    }

    @Test
    void removeParticipant_Fails_WhenRemovingSelf() {
        // Given
        UUID tripId = UUID.randomUUID();
        Trip trip = new Trip();
        trip.getParticipants().add(TripParticipant.builder().user(user).role(TripRole.OWNER).build());

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));

        // When & Then
        assertThrows(IllegalStateException.class, () -> tripService.removeParticipant(tripId, userId, token));
    }
}