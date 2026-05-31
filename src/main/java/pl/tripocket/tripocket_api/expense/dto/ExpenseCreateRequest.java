package pl.tripocket.tripocket_api.expense.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record ExpenseCreateRequest(
        @NotBlank @Size(max = 64)
        String category,

        @NotNull @Positive
        BigDecimal amount,

        @NotBlank @Size(min = 3, max = 3)
        String currency,

        @NotNull
        @JsonProperty("expenseDate")
        LocalDate expenseDate,

        @Size(max = 1000)
        String description,

        @NotNull
        @JsonProperty("paidByUserId")
        UUID paidByUserId,

        @NotEmpty @Valid
        List<ExpenseSplitRequest> splits
) {}
