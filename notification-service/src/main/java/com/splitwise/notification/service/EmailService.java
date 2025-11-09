package com.splitwise.notification.service;

import com.splitwise.notification.dto.*;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username:noreply@splitit.com}")
    private String fromEmail;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    /**
     * Send payment reminder email
     */
    public EmailResponse sendPaymentReminder(PaymentReminderRequest request) {
        try {
            log.info("Sending payment reminder to: {}", request.getDebtorEmail());

            Context context = new Context();
            context.setVariable("debtorName", request.getDebtorName());
            context.setVariable("creditorName", request.getCreditorName());
            context.setVariable("amount", request.getAmount());
            context.setVariable("currency", request.getCurrency() != null ? request.getCurrency() : "USD");
            context.setVariable("groupName", request.getGroupName());
            context.setVariable("notes", request.getNotes());
            context.setVariable("settleUpLink", baseUrl + "/groups/" + request.getGroupId() + "/settle");

            String htmlContent = templateEngine.process("payment-reminder", context);
            String subject = String.format("Payment Reminder: $%.2f owed to %s",
                    request.getAmount(), request.getCreditorName());

            sendHtmlEmail(request.getDebtorEmail(), subject, htmlContent);

            return EmailResponse.builder()
                    .success(true)
                    .message("Payment reminder sent successfully")
                    .sentAt(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error("Failed to send payment reminder: {}", e.getMessage(), e);
            return EmailResponse.builder()
                    .success(false)
                    .message("Failed to send payment reminder: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Send payment received notification
     */
    public EmailResponse sendPaymentReceived(PaymentReceivedRequest request) {
        try {
            log.info("Sending payment received notification to: {}", request.getPayeeEmail());

            Context context = new Context();
            context.setVariable("payeeName", request.getPayeeName());
            context.setVariable("payerName", request.getPayerName());
            context.setVariable("amount", request.getAmount());
            context.setVariable("currency", request.getCurrency() != null ? request.getCurrency() : "USD");
            context.setVariable("groupName", request.getGroupName());
            context.setVariable("paymentMethod", request.getPaymentMethod());
            context.setVariable("transactionId", request.getTransactionId());
            context.setVariable("viewLink", baseUrl + "/groups/" + request.getGroupId());
            context.setVariable("date", LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));

            String htmlContent = templateEngine.process("payment-received", context);
            String subject = String.format("Payment Received: $%.2f from %s",
                    request.getAmount(), request.getPayerName());

            sendHtmlEmail(request.getPayeeEmail(), subject, htmlContent);

            return EmailResponse.builder()
                    .success(true)
                    .message("Payment received notification sent successfully")
                    .sentAt(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error("Failed to send payment received notification: {}", e.getMessage(), e);
            return EmailResponse.builder()
                    .success(false)
                    .message("Failed to send payment received notification: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Send group invitation email
     */
    public EmailResponse sendGroupInvitation(GroupInvitationRequest request) {
        try {
            log.info("Sending group invitation to: {}", request.getInviteeEmail());

            Context context = new Context();
            context.setVariable("inviteeName", request.getInviteeName());
            context.setVariable("inviterName", request.getInviterName());
            context.setVariable("groupName", request.getGroupName());
            context.setVariable("groupDescription", request.getGroupDescription());
            context.setVariable("acceptLink", baseUrl + "/invitations/accept?token=" + request.getInvitationToken());
            context.setVariable("declineLink", baseUrl + "/invitations/decline?token=" + request.getInvitationToken());

            String htmlContent = templateEngine.process("group-invitation", context);
            String subject = String.format("You're invited to join %s on SplitIt", request.getGroupName());

            sendHtmlEmail(request.getInviteeEmail(), subject, htmlContent);

            return EmailResponse.builder()
                    .success(true)
                    .message("Group invitation sent successfully")
                    .sentAt(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error("Failed to send group invitation: {}", e.getMessage(), e);
            return EmailResponse.builder()
                    .success(false)
                    .message("Failed to send group invitation: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Helper method to send HTML email
     */
    private void sendHtmlEmail(String to, String subject, String htmlContent) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true); // true = HTML

        mailSender.send(message);
        log.info("Email sent successfully to: {}", to);
    }

    /**
     * Test email configuration
     */
    public EmailResponse sendTestEmail(String toEmail) {
        try {
            Context context = new Context();
            context.setVariable("message", "Your email configuration is working correctly!");

            String htmlContent = templateEngine.process("test-email", context);
            sendHtmlEmail(toEmail, "SplitIt Email Test", htmlContent);

            return EmailResponse.builder()
                    .success(true)
                    .message("Test email sent successfully")
                    .sentAt(LocalDateTime.now())
                    .build();
        } catch (Exception e) {
            log.error("Failed to send test email: {}", e.getMessage(), e);
            return EmailResponse.builder()
                    .success(false)
                    .message("Failed to send test email: " + e.getMessage())
                    .build();
        }
    }
}
