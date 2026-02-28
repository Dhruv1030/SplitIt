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
public class PaymentReceivedEmailRequest {
    private String payeeEmail;
    private String payeeName;
    private String payerName;
    private BigDecimal amount;
    private String groupName;
    private Long groupId;
    private String currency;
    private String paymentMethod;
    private String transactionId;
}
