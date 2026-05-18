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
        UUID tripId = UUID.randomUUID();
        UUID senderId = UUID.randomUUID();
        InvitationRequest request = new InvitationRequest("target_user", TripRole.PARTICIPANT);

        // 1. Przygotuj sendera
        User sender = new User();
        sender.setId(senderId);

        // 2. Przygotuj Trip z Senderem jako OWNEREM
        Trip trip = new Trip();
        trip.setId(tripId);
        trip.setParticipants(new ArrayList<>());

        TripParticipant owner = new TripParticipant();
        owner.setUser(sender);
        owner.setRole(TripRole.OWNER); // To jest kluczowe!
        trip.getParticipants().add(owner);

        User invitee = new User();
        invitee.setUsername("target_user");
        invitee.setId(UUID.randomUUID());

        // Mockowanie
        when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));
        when(userRepository.findById(senderId)).thenReturn(Optional.of(sender));
        when(userRepository.findByUsername("target_user")).thenReturn(Optional.of(invitee));
        when(invitationRepository.existsByTripIdAndInviteeIdAndStatus(any(), any(), any())).thenReturn(false);

        // When
        InvitationResponse response = invitationService.sendInvitation(tripId, request, senderId);

        // Then
        assertNotNull(response);
        verify(invitationRepository).save(any(Invitation.class));
    }

    @Test
    void sendInvitation_Fails_WhenAlreadyParticipant() {
        // 1. Given
        InvitationRequest request = new InvitationRequest("test_user", TripRole.PARTICIPANT);

        // trzeba przygotować sendera, który JEST właścicielem,
        // żeby kod nie wywalił się na checku isOwner
        User sender = new User();
        sender.setId(senderId);

        // Dodajemy sendera jako OWNERA do podróży
        TripParticipant owner = new TripParticipant();
        owner.setUser(sender);
        owner.setRole(TripRole.OWNER);
        owner.setTrip(trip);
        trip.getParticipants().add(owner);

        // Dodajemy osobę, która już jest w podróży (cel testu)
        TripParticipant existingParticipant = new TripParticipant();
        existingParticipant.setUser(inviteeUser);
        existingParticipant.setRole(TripRole.PARTICIPANT);
        trip.getParticipants().add(existingParticipant);

        // Mockowanie
        when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));
        when(userRepository.findById(senderId)).thenReturn(Optional.of(sender)); // Musi wrócić nasz przygotowany sender
        when(userRepository.findByUsername("test_user")).thenReturn(Optional.of(inviteeUser));

        // 2. When & Then
        // UWAGA: Teraz powinien lecieć IllegalStateException (bo user już jest w podróży),
        // a nie AccessDeniedException (błąd uprawnień).
        assertThrows(IllegalStateException.class, () ->
                invitationService.sendInvitation(tripId, request, senderId));
    }

    @Test
    void respond_Accept_AddsParticipantToTrip() {
        // 1. Given (Przygotowanie danych przez settery)
        UUID invitationId = UUID.randomUUID();
        UUID inviteeId = UUID.randomUUID();

        // Mockujemy token JWT
        JwtAuthenticationToken mockToken = mock(JwtAuthenticationToken.class);
        when(mockToken.getName()).thenReturn(inviteeId.toString());

        // Tworzymy zaproszonego użytkownika
        User inviteeUser = new User();
        inviteeUser.setId(inviteeId);
        inviteeUser.setUsername("invitee_username");

        // Tworzymy podróż
        Trip trip = new Trip();
        trip.setId(UUID.randomUUID());
        trip.setName("Test Trip");
        trip.setParticipants(new ArrayList<>()); // Inicjalizacja listy uczestników

        // Tworzymy zaproszenie
        Invitation invitation = new Invitation();
        invitation.setId(invitationId);
        invitation.setTrip(trip);
        invitation.setInvitee(inviteeUser);
        invitation.setStatus(InvitationStatus.PENDING);

        InvitationRespondRequest request = new InvitationRespondRequest(true);

        when(invitationRepository.findById(invitationId)).thenReturn(Optional.of(invitation));

        // 2. When
        invitationService.respond(invitationId, request, mockToken);

        // 3. Then
        assertEquals(InvitationStatus.ACCEPTED, invitation.getStatus());

        // Weryfikacja dodania uczestnika
        assertEquals(1, trip.getParticipants().size());
        TripParticipant addedParticipant = trip.getParticipants().get(0);
        assertEquals(inviteeUser, addedParticipant.getUser());
        assertEquals(TripRole.PARTICIPANT, addedParticipant.getRole());

        verify(tripRepository).save(trip);
        verify(invitationRepository).save(invitation);
    }

    @Test
    void respond_ThrowsException_WhenStrangerTriesToRespond() {
        // Given
        UUID invitationId = UUID.randomUUID();
        UUID actualInviteeId = UUID.randomUUID();
        UUID intruderId = UUID.randomUUID(); // ID kogoś, kto nie jest adresatem

        // Mockujemy token "hakera"
        JwtAuthenticationToken intruderToken = mock(JwtAuthenticationToken.class);
        when(intruderToken.getName()).thenReturn(intruderId.toString());

        // Przygotowujemy zaproszenie wystawione dla kogoś innego (actualInviteeId)
        User actualInvitee = new User();
        actualInvitee.setId(actualInviteeId);

        Invitation invitation = new Invitation();
        invitation.setInvitee(actualInvitee);
        invitation.setStatus(InvitationStatus.PENDING);

        when(invitationRepository.findById(invitationId)).thenReturn(Optional.of(invitation));

        // When & Then
        org.springframework.security.access.AccessDeniedException exception = assertThrows(
                org.springframework.security.access.AccessDeniedException.class,
                () -> invitationService.respond(invitationId, new InvitationRespondRequest(true), intruderToken)
        );

        // Weryfikujemy, czy na pewno rzucił błąd z metody respond, a nie z innej metody
        assertEquals("Nie możesz zaakceptować cudzego zaproszenia.", exception.getMessage());
    }
}
