package pl.tripocket.tripocket_api.expense.model;

import jakarta.persistence.*;
import lombok.*;
import pl.tripocket.tripocket_api.auth.user.model.User;

import java.math.BigDecimal;

@Entity
@Table(name = "expense_splits")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseSplit {

    @EmbeddedId
    @Builder.Default
    private ExpenseSplitId id = new ExpenseSplitId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("expenseId")
    @JoinColumn(name = "expense_id")
    private Expense expense;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * Wyliczona kwota długu uczestnika wobec osoby, która zapłaciła za wydatek
     * (w oryginalnej walucie wydatku).
     */
    @Column(name = "owed_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal owedAmount;
}
