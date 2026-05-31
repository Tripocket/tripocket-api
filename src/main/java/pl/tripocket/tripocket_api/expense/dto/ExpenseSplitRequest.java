package pl.tripocket.tripocket_api.expense.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record ExpenseSplitRequest(
        @NotNull
        UUID userId,

        @Positive
        BigDecimal share
) {}
