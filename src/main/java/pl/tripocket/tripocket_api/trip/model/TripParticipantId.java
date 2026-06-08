package pl.tripocket.tripocket_api.trip.model;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.util.UUID;

//klasa pomocnicza dla klucza złożonego
//żeby auto mapowało z rolami potem
@Embeddable
@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class TripParticipantId implements java.io.Serializable {
    private UUID tripId;
    private UUID userId;
}
