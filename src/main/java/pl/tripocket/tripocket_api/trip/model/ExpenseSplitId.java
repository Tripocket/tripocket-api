package pl.tripocket.tripocket_api.trip.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ExpenseSplitId implements Serializable {

  @Serial private static final long serialVersionUID = 1L;

  @Column(name = "expense_id")
  private UUID expenseId;

  @Column(name = "user_id")
  private UUID userId;
}
