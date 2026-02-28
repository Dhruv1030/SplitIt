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

    // What the current user still owes: their own unpaid splits on expenses someone
    // else paid
    @Query("SELECT SUM(s.amount) FROM ExpenseSplit s WHERE s.userId = :userId AND s.isPaid = false AND s.expense.isActive = true AND s.expense.paidBy <> :userId")
    BigDecimal getTotalOwedByUser(@Param("userId") String userId);

    // What others still owe the current user: unpaid splits of other people on
    // expenses this user paid
    @Query("SELECT SUM(s.amount) FROM ExpenseSplit s WHERE s.expense.paidBy = :userId AND s.userId <> :userId AND s.isPaid = false AND s.expense.isActive = true")
    BigDecimal getTotalOwedToUser(@Param("userId") String userId);
}
