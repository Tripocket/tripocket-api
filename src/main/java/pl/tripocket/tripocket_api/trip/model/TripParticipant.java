package pl.tripocket.tripocket_api.trip.model;

import jakarta.persistence.*;
import lombok.*;
import pl.tripocket.tripocket_api.auth.user.model.User;
import java.time.Instant;

@Entity
@Table(name = "trip_participants")
@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripParticipant {

    @EmbeddedId
    @Builder.Default
    private TripParticipantId id = new TripParticipantId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("tripId")
    @JoinColumn(name = "trip_id")
    private Trip trip;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    private TripRole role; // Ujednolicone do OWNER/PARTICIPANT

    @Builder.Default
    @Column(name = "joined_at")
    private Instant joinedAt = Instant.now();
}