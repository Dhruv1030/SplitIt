package com.splitwise.settlement.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailNotificationClient {

    private final RestTemplate restTemplate;
    private static final String NOTIFICATION_SERVICE_URL = "http://notification-service:8085/api/notifications";

    /**
     * Send payment received notification email
     */
    public void sendPaymentReceivedEmail(PaymentReceivedEmailRequest request) {
        try {
            log.info("Sending payment received email to: {}", request.getPayeeEmail());

            String url = NOTIFICATION_SERVICE_URL + "/payment-received";
            restTemplate.postForObject(url, request, Object.class);

            log.info("Payment received email sent successfully");
        } catch (Exception e) {
            log.error("Failed to send payment received email: {}", e.getMessage(), e);
            // Don't throw exception - email failure shouldn't break the flow
        }
    }

    /**
     * Send payment reminder email
     */
    public void sendPaymentReminderEmail(PaymentReminderEmailRequest request) {
        try {
            log.info("Sending payment reminder email to: {}", request.getDebtorEmail());

            String url = NOTIFICATION_SERVICE_URL + "/payment-reminder";
            restTemplate.postForObject(url, request, Object.class);

            log.info("Payment reminder email sent successfully");
        } catch (Exception e) {
            log.error("Failed to send payment reminder email: {}", e.getMessage(), e);
            // Don't throw exception - email failure shouldn't break the flow
        }
    }
}
