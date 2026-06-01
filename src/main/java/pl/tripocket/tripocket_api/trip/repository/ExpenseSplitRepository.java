package pl.tripocket.tripocket_api.trip.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.tripocket.tripocket_api.trip.model.ExpenseSplit;
import pl.tripocket.tripocket_api.trip.model.ExpenseSplitId;

public interface ExpenseSplitRepository extends JpaRepository<ExpenseSplit, ExpenseSplitId> {

  @Query(
      """
      SELECT es.user.id as debtorId, es.user.username as debtorUsername,
      e.payer.id as creditorId, e.payer.username as creditorUsername,
      SUM(es.owedAmount) as totalAmount
      FROM ExpenseSplit es JOIN es.expense e
      WHERE e.trip.id = :tripId AND es.user.id <> e.payer.id
      GROUP BY es.user.id, es.user.username, e.payer.id, e.payer.username
  """)
  List<DebtEntry> findGrossDebtsByTripId(@Param("tripId") UUID tripId);

  interface DebtEntry {
    UUID getDebtorId();

    String getDebtorUsername();

    UUID getCreditorId();

    String getCreditorUsername();

    BigDecimal getTotalAmount();
  }
}
