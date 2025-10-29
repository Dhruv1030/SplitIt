package com.splitwise.settlement.dto;

import com.splitwise.settlement.entity.SettlementStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response for settlement record
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SettlementResponse {

    private Long id;
    private Long groupId;
    private String payerId;
    private String payeeId;
    private BigDecimal amount;
    private String currency;
    private SettlementStatus status;
    private String paymentMethod;
    private String transactionId;
    private String notes;
    private LocalDateTime settledAt;
    private LocalDateTime createdAt;
}
