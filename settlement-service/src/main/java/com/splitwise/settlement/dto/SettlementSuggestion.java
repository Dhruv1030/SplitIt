package com.splitwise.settlement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Represents a suggested settlement transaction between two users
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SettlementSuggestion {

    private String payerId; // User who should pay
    private String payerName; // Name for display
    private String payeeId; // User who should receive
    private String payeeName; // Name for display
    private BigDecimal amount; // Amount to be paid
    private String currency;

    public String getDescription() {
        return String.format("%s pays %s %s %.2f",
                payerName, payeeName, currency, amount);
    }
}
