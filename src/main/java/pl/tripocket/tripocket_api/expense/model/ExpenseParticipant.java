package pl.tripocket.tripocket_api.expense.model;

import jakarta.persistence.*;
import lombok.*;
import pl.tripocket.tripocket_api.auth.user.model.User;

@Entity
@Table(name = "expense_participants")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseParticipant {

    @EmbeddedId
    @Builder.Default
    private ExpenseParticipantId id = new ExpenseParticipantId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("expenseId")
    @JoinColumn(name = "expense_id")
    private Expense expense;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;
}