package pl.tripocket.tripocket_api.expense.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.tripocket.tripocket_api.expense.model.Expense;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, UUID> {

  List<Expense> findAllByTripId(UUID tripId);

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