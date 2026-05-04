package pl.tripocket.tripocket_api.trip;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.tripocket.tripocket_api.auth.user.model.User;
import pl.tripocket.tripocket_api.auth.user.repository.UserRepository;
import pl.tripocket.tripocket_api.common.exception.ResourceNotFoundException;
import pl.tripocket.tripocket_api.trip.dto.InviteRequest;
import pl.tripocket.tripocket_api.trip.model.Trip;
import pl.tripocket.tripocket_api.trip.model.TripParticipant;
import pl.tripocket.tripocket_api.trip.repository.TripRepository;
import pl.tripocket.tripocket_api.trip.service.TripServiceImpl;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TripServiceTest {

    @Mock
    private TripRepository tripRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TripServiceImpl tripService;

    private User ownerUser;
    private UUID tripId;
    private Trip testTrip;

    @BeforeEach
    void setUp() {
        tripId = UUID.randomUUID();

        ownerUser = new User();
        ownerUser.setId(UUID.randomUUID());
        ownerUser.setUsername("owner_user");

        testTrip = new Trip();
        testTrip.setId(tripId);
        testTrip.setParticipants(new ArrayList<>());

        // Dodajemy właściciela do podróży (symulacja istniejącej podróży)
        TripParticipant ownerPart = new TripParticipant();
        ownerPart.setUser(ownerUser);
        ownerPart.setRole("OWNER");
        testTrip.getParticipants().add(ownerPart);
    }

    @Test
    @DisplayName("Should throw exception when creator is not found")
    void shouldThrowExceptionWhenUserNotFound() {
        // GIVEN
        UUID nonExistentId = UUID.randomUUID();
        when(userRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThrows(ResourceNotFoundException.class, () ->
                tripService.createTrip(null, nonExistentId)
        );
    }

    @Test
    @DisplayName("WF-07/08: Should successfully invite a new participant")
    void shouldInviteParticipantSuccessfully() {
        // GIVEN
        User friend = new User();
        friend.setId(UUID.randomUUID());
        friend.setUsername("friend_user");

        InviteRequest inviteRequest = new InviteRequest("friend_user", "PARTICIPANT");

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(testTrip));
        when(userRepository.findByUsername("friend_user")).thenReturn(Optional.of(friend));

        // WHEN
        tripService.inviteUser(tripId, inviteRequest, ownerUser.getId());

        // THEN
        assertEquals(2, testTrip.getParticipants().size());
        boolean invitedExists = testTrip.getParticipants().stream()
                .anyMatch(p -> p.getUser().getUsername().equals("friend_user") && "PARTICIPANT".equals(p.getRole()));
        assertTrue(invitedExists);
        verify(tripRepository).save(testTrip);
    }

    @Test
    @DisplayName("Should block invitation if requester is not the owner")
    void shouldBlockInvitationForNonOwner() {
        // GIVEN
        UUID hackerId = UUID.randomUUID(); // Ktoś inny
        InviteRequest inviteRequest = new InviteRequest("some_user", "PARTICIPANT");

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(testTrip));

        // WHEN & THEN
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                tripService.inviteUser(tripId, inviteRequest, hackerId)
        );
        assertEquals("Only owner can invite others", exception.getMessage());
    }

    @Test
    @DisplayName("Should successfully remove a participant")
    void shouldRemoveParticipant() {
        // GIVEN
        UUID friendId = UUID.randomUUID();
        User friend = new User();
        friend.setId(friendId);

        TripParticipant friendPart = new TripParticipant();
        friendPart.setUser(friend);
        friendPart.setRole("PARTICIPANT");
        testTrip.getParticipants().add(friendPart);

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(testTrip));

        // WHEN
        tripService.removeParticipant(tripId, friendId, ownerUser.getId());

        // THEN
        assertEquals(1, testTrip.getParticipants().size()); // Został tylko owner
        verify(tripRepository).save(testTrip);
    }

    @Test
    @DisplayName("Should prevent owner from removing themselves")
    void shouldPreventOwnerFromSelfRemoval() {
        // GIVEN
        when(tripRepository.findById(tripId)).thenReturn(Optional.of(testTrip));

        // WHEN & THEN
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                tripService.removeParticipant(tripId, ownerUser.getId(), ownerUser.getId())
        );
        assertEquals("You cannot remove yourself from the trip", exception.getMessage());
    }
}