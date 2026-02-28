package com.splitwise.notification.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentReceivedRequest {

    @NotBlank(message = "Payee email is required")
    @Email(message = "Invalid payee email format")
    private String payeeEmail;

    @NotBlank(message = "Payee name is required")
    private String payeeName;

    @NotBlank(message = "Payer name is required")
    private String payerName;

    @NotNull(message = "Amount is required")
    private BigDecimal amount;

    @NotBlank(message = "Group name is required")
    private String groupName;

    private Long groupId;
    private String currency;
    private String paymentMethod;
    private String transactionId;
}
