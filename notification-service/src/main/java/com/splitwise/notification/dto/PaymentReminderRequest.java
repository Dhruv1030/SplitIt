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
public class PaymentReminderRequest {

    @NotBlank(message = "Debtor email is required")
    @Email(message = "Invalid debtor email format")
    private String debtorEmail;

    @NotBlank(message = "Debtor name is required")
    private String debtorName;

    @NotBlank(message = "Creditor name is required")
    private String creditorName;

    @NotNull(message = "Amount is required")
    private BigDecimal amount;

    @NotBlank(message = "Group name is required")
    private String groupName;

    private Long groupId;
    private String currency;
    private String notes;
}
