package pl.tripocket.tripocket_api.expense.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import pl.tripocket.tripocket_api.expense.model.RateSource;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record ExpenseResponse(
        UUID id,
        @JsonProperty("tripId") UUID tripId,
        String category,
        BigDecimal amount,
        String currency,
        @JsonProperty("exchangeRate") BigDecimal exchangeRate,
        @JsonProperty("rateSource") RateSource rateSource,
        @JsonProperty("expenseDate") LocalDate expenseDate,
        String description,
        ExpenseParticipantResponse paidBy,
        List<ExpenseSplitResponse> splits,
        @JsonProperty("createdAt") Instant createdAt
) {}