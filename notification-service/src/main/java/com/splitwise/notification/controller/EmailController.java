package com.splitwise.notification.controller;

import com.splitwise.notification.dto.*;
import com.splitwise.notification.service.EmailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Email Notifications", description = "APIs for sending email notifications (payment reminders, confirmations, invitations)")
public class EmailController {

    private final EmailService emailService;

    @Operation(summary = "Send payment reminder email", description = "Send a payment reminder email to a debtor with outstanding payment details")
    @PostMapping("/payment-reminder")
    public ResponseEntity<EmailResponse> sendPaymentReminder(@Valid @RequestBody PaymentReminderRequest request) {
        log.info("Received payment reminder request for debtor: {}", request.getDebtorEmail());
        EmailResponse response = emailService.sendPaymentReminder(request);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(500).body(response);
        }
    }

    @Operation(summary = "Send payment received confirmation", description = "Send a payment received confirmation email to the payee")
    @PostMapping("/payment-received")
    public ResponseEntity<EmailResponse> sendPaymentReceived(@Valid @RequestBody PaymentReceivedRequest request) {
        log.info("Received payment received notification request for payee: {}", request.getPayeeEmail());
        EmailResponse response = emailService.sendPaymentReceived(request);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(500).body(response);
        }
    }

    @Operation(summary = "Send group invitation email", description = "Send an invitation email to a new group member")
    @PostMapping("/group-invitation")
    public ResponseEntity<EmailResponse> sendGroupInvitation(@Valid @RequestBody GroupInvitationRequest request) {
        log.info("Received group invitation request for invitee: {}", request.getInviteeEmail());
        EmailResponse response = emailService.sendGroupInvitation(request);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(500).body(response);
        }
    }

    @Operation(summary = "Send test email", description = "Send a test email to verify SMTP configuration")
    @PostMapping("/test")
    public ResponseEntity<EmailResponse> sendTestEmail(@RequestParam String email) {
        log.info("Received test email request for: {}", email);
        EmailResponse response = emailService.sendTestEmail(email);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(500).body(response);
        }
    }

    @Operation(summary = "Health check", description = "Check if the email notification service is running")
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Email notification service is running");
    }
}
