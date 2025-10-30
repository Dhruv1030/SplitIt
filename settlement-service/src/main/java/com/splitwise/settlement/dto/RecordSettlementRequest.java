package com.splitwise.settlement.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    @NotNull(message = "Group ID is required")
    private Long groupId;

    @NotBlank(message = "Payer ID is required")
    private String payerId;

    @NotBlank(message = "Payee ID is required")
    private String payeeId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    private String currency;

    @NotBlank(message = "Payment method is required")
    private String paymentMethod; // CASH, UPI, BANK_TRANSFER, CREDIT_CARD

    private String transactionId;
    private String notes;
}
