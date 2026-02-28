package com.splitwise.settlement.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentReminderEmailRequest {
    private String debtorEmail;
    private String debtorName;
    private String creditorName;
    private BigDecimal amount;
    private String groupName;
    private Long groupId;
    private String currency;
    private String notes;
}
