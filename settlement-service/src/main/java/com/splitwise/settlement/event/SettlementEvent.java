package com.splitwise.settlement.event;

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
public class SettlementEvent {
    private String eventType; // SETTLEMENT_CREATED, SETTLEMENT_COMPLETED
    private Long settlementId;
    private String payerUserId;
    private String payeeUserId;
    private BigDecimal amount;
    private Long groupId;
    private LocalDateTime timestamp;
}
