package pl.tripocket.tripocket_api.expense.dto;

import java.util.UUID;

public record ExpenseParticipantResponse(
        UUID id,
        String username,
        String displayName,
        String email
) {}