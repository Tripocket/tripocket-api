package pl.tripocket.tripocket_api.expense.model;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class ExpenseParticipantId implements Serializable {
    private UUID expenseId;
    private UUID userId;
}