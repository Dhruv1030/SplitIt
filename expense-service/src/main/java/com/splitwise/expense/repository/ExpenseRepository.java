package com.splitwise.expense.repository;

import com.splitwise.expense.model.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    Optional<Expense> findByIdAndIsActiveTrue(Long id);

    List<Expense> findByGroupIdAndIsActiveTrueOrderByDateDesc(Long groupId);

    @Query("SELECT e FROM Expense e JOIN e.splits s WHERE s.userId = :userId AND e.isActive = true ORDER BY e.date DESC")
    List<Expense> findByUserIdOrderByDateDesc(@Param("userId") String userId);

    @Query("SELECT e FROM Expense e WHERE e.groupId = :groupId AND e.paidBy = :userId AND e.isActive = true")
    List<Expense> findByGroupIdAndPaidBy(@Param("groupId") Long groupId, @Param("userId") String userId);

    @Query("SELECT COUNT(e) FROM Expense e WHERE e.groupId = :groupId AND e.isActive = true")
    int countByGroupId(@Param("groupId") Long groupId);

    /**
     * Find all friend expenses between two users (direct, non-group)
     */
    @Query("SELECT e FROM Expense e WHERE e.expenseType = com.splitwise.expense.model.ExpenseType.FRIEND " +
            "AND e.isActive = true " +
            "AND ((e.paidBy = :userId AND e.friendUserId = :friendId) " +
            "OR (e.paidBy = :friendId AND e.friendUserId = :userId)) " +
            "ORDER BY e.date DESC")
    List<Expense> findFriendExpenses(@Param("userId") String userId, @Param("friendId") String friendId);

    /**
     * Find ALL expenses where both users are involved (group + friend),
     * used for calculating total net balance between two users
     */
    @Query("SELECT DISTINCT e FROM Expense e JOIN e.splits s WHERE e.isActive = true " +
            "AND ((e.paidBy = :userId AND s.userId = :friendId) " +
            "OR (e.paidBy = :friendId AND s.userId = :userId)) " +
            "ORDER BY e.date DESC")
    List<Expense> findAllExpensesBetweenUsers(@Param("userId") String userId, @Param("friendId") String friendId);
}
