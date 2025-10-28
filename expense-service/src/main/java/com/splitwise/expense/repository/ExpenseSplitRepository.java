package com.splitwise.expense.repository;

import com.splitwise.expense.model.ExpenseSplit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ExpenseSplitRepository extends JpaRepository<ExpenseSplit, Long> {

    List<ExpenseSplit> findByExpenseId(Long expenseId);

    List<ExpenseSplit> findByUserId(String userId);

    @Query("SELECT SUM(s.amount) FROM ExpenseSplit s WHERE s.userId = :userId")
    BigDecimal getTotalOwedByUser(@Param("userId") String userId);

    @Query("SELECT SUM(e.amount) FROM Expense e WHERE e.paidBy = :userId AND e.isActive = true")
    BigDecimal getTotalPaidByUser(@Param("userId") String userId);
}
