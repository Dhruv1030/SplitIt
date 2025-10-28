package com.splitwise.expense.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserBalanceResponse {

    private String userId;
    private BigDecimal totalPaid;
    private BigDecimal totalOwed;
    private BigDecimal netBalance; // Positive = others owe you, Negative = you owe others

    // Map of userId -> amount (how much each person owes you or you owe them)
    private Map<String, BigDecimal> balances;
}
