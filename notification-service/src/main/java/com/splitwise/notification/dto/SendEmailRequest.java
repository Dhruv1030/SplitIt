package com.splitwise.notification.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendEmailRequest {

    @NotBlank(message = "Recipient email is required")
    @Email(message = "Invalid email format")
    private String to;

    @NotBlank(message = "Subject is required")
    private String subject;

    @NotBlank(message = "Email type is required")
    private String emailType; // EXPENSE_CREATED, PAYMENT_REMINDER, etc.

    // Template variables for dynamic content
    private Map<String, Object> templateData;

    // Optional fields
    private String cc;
    private String bcc;
}
