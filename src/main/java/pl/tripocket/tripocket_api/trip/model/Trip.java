package pl.tripocket.tripocket_api.trip.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "trips")
@Getter @Setter
public class Trip {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String country;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal budget;

    //mapowanie do es kiu el

    @Column(name = "currency_primary")
    private String currencyPrimary;

    @Column(name = "currency_secondary")
    private String currencySecondary;

    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TripParticipant> participants = new java.util.ArrayList<>();
}