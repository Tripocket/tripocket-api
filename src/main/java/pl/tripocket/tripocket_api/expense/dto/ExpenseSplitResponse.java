package pl.tripocket.tripocket_api.expense.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record ExpenseSplitResponse(
        ExpenseParticipantResponse user,
        @JsonProperty("owedAmount") BigDecimal owedAmount
) {}
