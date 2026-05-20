package pl.tripocket.tripocket_api.trip.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.tripocket.tripocket_api.trip.model.Expense;

public interface ExpenseRepository extends JpaRepository<Expense, UUID> {

  @Query(
      """
      SELECT e.category as category, SUM(e.amount) as total
      FROM Expense e
      WHERE e.trip.id = :tripId
      GROUP BY e.category
  """)
  List<CategoryTotal> findCategoryTotalsByTripId(@Param("tripId") UUID tripId);

  interface CategoryTotal {
    String getCategory();

    BigDecimal getTotal();
  }
}
