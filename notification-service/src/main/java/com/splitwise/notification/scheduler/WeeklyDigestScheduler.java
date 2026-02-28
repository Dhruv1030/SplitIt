package com.splitwise.notification.scheduler;

import com.splitwise.notification.dto.PaymentReminderRequest;
import com.splitwise.notification.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class WeeklyDigestScheduler {

    private final EmailService emailService;
    private final RestTemplate restTemplate;

    /**
     * Weekly payment reminder digest
     * Runs every Monday at 9:00 AM
     * Cron format: second minute hour day month weekday
     * 0 0 9 * * MON = 9:00 AM every Monday
     */
    @Scheduled(cron = "0 0 9 * * MON")
    public void sendWeeklyPaymentReminders() {
        log.info("Starting weekly payment reminder digest at {}", LocalDateTime.now());

        try {
            // Fetch outstanding settlements from settlement-service
            String settlementServiceUrl = "http://settlement-service:8084/api/settlements/outstanding";

            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                    settlementServiceUrl,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<Map<String, Object>>>() {
                    });

            List<Map<String, Object>> outstandingSettlements = response.getBody();

            if (outstandingSettlements == null || outstandingSettlements.isEmpty()) {
                log.info("No outstanding settlements found. Weekly digest skipped.");
                return;
            }

            log.info("Found {} outstanding settlements to process", outstandingSettlements.size());

            int emailsSent = 0;
            int emailsFailed = 0;

            // Send reminder email for each outstanding settlement
            for (Map<String, Object> settlement : outstandingSettlements) {
                try {
                    sendReminderForSettlement(settlement);
                    emailsSent++;
                } catch (Exception e) {
                    log.error("Failed to send reminder for settlement {}: {}",
                            settlement.get("id"), e.getMessage());
                    emailsFailed++;
                }
            }

            log.info("Weekly digest completed. Emails sent: {}, Failed: {}", emailsSent, emailsFailed);

        } catch (Exception e) {
            log.error("Error during weekly payment reminder digest: {}", e.getMessage(), e);
        }
    }

    /**
     * Send payment reminder for a single settlement
     */
    private void sendReminderForSettlement(Map<String, Object> settlement) {
        try {
            // Extract settlement details
            String debtorEmail = (String) settlement.get("debtorEmail");
            String debtorName = (String) settlement.get("debtorName");
            String creditorName = (String) settlement.get("creditorName");
            Double amountDouble = ((Number) settlement.get("amount")).doubleValue();
            BigDecimal amount = BigDecimal.valueOf(amountDouble);
            String currency = (String) settlement.getOrDefault("currency", "USD");
            String groupName = (String) settlement.get("groupName");
            Long groupId = ((Number) settlement.get("groupId")).longValue();

            // Build payment reminder request
            PaymentReminderRequest request = PaymentReminderRequest.builder()
                    .debtorEmail(debtorEmail)
                    .debtorName(debtorName)
                    .creditorName(creditorName)
                    .amount(amount)
                    .currency(currency)
                    .groupName(groupName)
                    .groupId(groupId)
                    .notes("This is your weekly reminder for pending payment. Please settle when convenient.")
                    .build();

            // Send email
            emailService.sendPaymentReminder(request);
            log.debug("Sent weekly reminder to {} for {} {}", debtorEmail, amount, currency);

        } catch (Exception e) {
            log.error("Error sending reminder for settlement: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Manual trigger for testing (every 2 minutes during development)
     * Comment out in production or change cron expression
     */
    // @Scheduled(cron = "0 */2 * * * *")
    public void sendWeeklyPaymentRemindersTest() {
        log.info("TEST: Manual trigger for weekly payment reminder digest");
        sendWeeklyPaymentReminders();
    }
}
