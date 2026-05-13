package pl.tripocket.tripocket_api.trip.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import pl.tripocket.tripocket_api.auth.user.model.User;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "invitations")
@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Invitation {
    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inviter_id", nullable = false)
    private User inviter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invitee_id", nullable = false)
    private User invitee;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private InvitationStatus status = InvitationStatus.PENDING;

    @Column(name = "created_at")
    @Builder.Default
    private Instant createdAt = Instant.now();
}

