package com.splitwise.settlement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request to record a settlement
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecordSettlementRequest {

    private Long groupId;
    private String payerId;
    private String payeeId;
    private BigDecimal amount;
    private String currency;
    private String paymentMethod; // CASH, UPI, BANK_TRANSFER, CREDIT_CARD
    private String transactionId;
    private String notes;
}
