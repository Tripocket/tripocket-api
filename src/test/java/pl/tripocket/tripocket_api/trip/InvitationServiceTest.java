package pl.tripocket.tripocket_api.trip;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.tripocket.tripocket_api.auth.user.model.User;
import pl.tripocket.tripocket_api.auth.user.repository.UserRepository;
import pl.tripocket.tripocket_api.common.exception.ResourceNotFoundException;
import pl.tripocket.tripocket_api.trip.dto.*;
import pl.tripocket.tripocket_api.trip.model.*;
import pl.tripocket.tripocket_api.trip.repository.InvitationRepository;
import pl.tripocket.tripocket_api.trip.repository.TripRepository;
import pl.tripocket.tripocket_api.trip.service.InvitationService;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvitationServiceTest {

    @Mock private InvitationRepository invitationRepository;
    @Mock private TripRepository tripRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks private InvitationService invitationService;

    private UUID tripId;
    private UUID senderId;
    private User inviteeUser;
    private Trip trip;

    @BeforeEach
    void setUp() {
        tripId = UUID.randomUUID();
        senderId = UUID.randomUUID();

        inviteeUser = new User();
        inviteeUser.setId(UUID.randomUUID());
        inviteeUser.setUsername("test_user");

        trip = new Trip();
        trip.setId(tripId);
        trip.setParticipants(new ArrayList<>());
    }

    @Test
    void sendInvitation_Success() {
        // Given
        InvitationRequest request = new InvitationRequest("test_user", "PARTICIPANT");
        User sender = new User();
        sender.setId(senderId);

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));
        when(userRepository.findById(senderId)).thenReturn(Optional.of(sender));
        when(userRepository.findByUsername("test_user")).thenReturn(Optional.of(inviteeUser));
        when(invitationRepository.existsByTripIdAndInviteeIdAndStatus(any(), any(), any())).thenReturn(false);

        // When
        InvitationResponse response = invitationService.sendInvitation(tripId, request, senderId);

        // Then
        assertNotNull(response);
        assertTrue(response.message().contains("test_user"));
        verify(invitationRepository, times(1)).save(any(Invitation.class));
    }

    @Test
    void sendInvitation_Fails_WhenAlreadyParticipant() {
        // Given
        InvitationRequest request = new InvitationRequest("test_user", "PARTICIPANT");
        TripParticipant participant = TripParticipant.builder().user(inviteeUser).build();
        trip.getParticipants().add(participant);

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));
        when(userRepository.findById(senderId)).thenReturn(Optional.of(new User()));
        when(userRepository.findByUsername("test_user")).thenReturn(Optional.of(inviteeUser));

        // When & Then
        assertThrows(IllegalStateException.class, () ->
                invitationService.sendInvitation(tripId, request, senderId));
    }

    @Test
    void respond_Accept_AddsParticipantToTrip() {
        // Given
        UUID invId = UUID.randomUUID();
        Invitation invitation = Invitation.builder()
                .id(invId)
                .trip(trip)
                .invitee(inviteeUser)
                .status(InvitationStatus.PENDING)
                .build();

        InvitationRespondRequest request = new InvitationRespondRequest(true);

        when(invitationRepository.findById(invId)).thenReturn(Optional.of(invitation));

        // When
        invitationService.respond(invId, request);

        // Then
        assertEquals(InvitationStatus.ACCEPTED, invitation.getStatus());
        assertEquals(1, trip.getParticipants().size());
        assertEquals(inviteeUser, trip.getParticipants().get(0).getUser());
        verify(tripRepository).save(trip);
    }
}
