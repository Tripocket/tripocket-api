package pl.tripocket.tripocket_api.trip.model;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.tripocket.tripocket_api.auth.user.model.User;

@Entity
@Table(name = "expense_splits")
@Getter
@Setter
@NoArgsConstructor
public class ExpenseSplit {

  @EmbeddedId
  private ExpenseSplitId id;

  @ManyToOne(fetch = FetchType.LAZY)
  @MapsId("expenseId")
  @JoinColumn(name = "expense_id")
  private Expense expense;

  @ManyToOne(fetch = FetchType.LAZY)
  @MapsId("userId")
  @JoinColumn(name = "user_id")
  private User user;

  @Column(name = "owed_amount", nullable = false, precision = 19, scale = 2)
  private BigDecimal owedAmount;
}
