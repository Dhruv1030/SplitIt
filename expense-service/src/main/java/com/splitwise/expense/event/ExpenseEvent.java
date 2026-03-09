package com.splitwise.expense.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseEvent {
    private String eventType; // EXPENSE_CREATED, EXPENSE_UPDATED, EXPENSE_DELETED
    private Long expenseId;
    private String description;
    private BigDecimal amount;
    private String paidByUserId;
    private Long groupId;
    private String category;
    private LocalDateTime timestamp;
}
