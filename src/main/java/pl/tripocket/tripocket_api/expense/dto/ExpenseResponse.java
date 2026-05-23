package pl.tripocket.tripocket_api.expense.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

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
        @JsonProperty("expenseDate") LocalDate expenseDate,
        String description,
        ExpenseParticipantResponse paidBy,
        List<ExpenseParticipantResponse> participants,
        @JsonProperty("createdAt") Instant createdAt
) {}