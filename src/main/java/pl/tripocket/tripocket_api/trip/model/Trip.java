package pl.tripocket.tripocket_api.trip.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "trips")
@Getter @Setter
public class Trip {
    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_trip_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Trip parentTrip;

    private String name;
    private String country;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal budget;
    private String baseCurrency;
    private String transportMode;
    private String tripType;

    @Enumerated(EnumType.STRING) // Zmienione ze String na Enum
    private TripStatus status;

    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TripParticipant> participants = new ArrayList<>();

    @OneToMany(mappedBy = "parentTrip", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Trip> subTrips = new ArrayList<>();
}