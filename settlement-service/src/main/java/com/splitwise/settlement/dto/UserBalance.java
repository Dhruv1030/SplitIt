package com.splitwise.settlement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

/**
 * User balance information
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserBalance {

    private String userId;
    private BigDecimal netBalance; // Positive = others owe you, Negative = you owe others
    private Map<String, BigDecimal> balances; // userId -> amount
}
