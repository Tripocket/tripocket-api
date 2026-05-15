package pl.tripocket.tripocket_api.trip.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import pl.tripocket.tripocket_api.trip.model.TripStatus;
import java.math.BigDecimal;
import java.time.LocalDate;

public record TripUpdateRequest(
        @Size(min=0, max=255) String name,
        @Size(min=0, max=127)String country,
        LocalDate startDate,
        LocalDate endDate,
        @Positive BigDecimal budget,
        @Size(min=0, max=3) String baseCurrency,
        @Size(min=0, max=64) String transportMode,
        @Size(min=0, max=64) String tripType,
        TripStatus status
) {}