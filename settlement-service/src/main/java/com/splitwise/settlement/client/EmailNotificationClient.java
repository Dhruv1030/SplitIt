package com.splitwise.settlement.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
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
    @CircuitBreaker(name = "notificationService", fallbackMethod = "sendPaymentReceivedEmailFallback")
    public void sendPaymentReceivedEmail(PaymentReceivedEmailRequest request) {
        log.info("Sending payment received email to: {}", request.getPayeeEmail());
        String url = NOTIFICATION_SERVICE_URL + "/payment-received";
        restTemplate.postForObject(url, request, Object.class);
        log.info("Payment received email sent successfully");
    }

    public void sendPaymentReceivedEmailFallback(PaymentReceivedEmailRequest request, Throwable t) {
        log.warn("Circuit breaker fallback for sendPaymentReceivedEmail: {}", t.getMessage());
    }

    @CircuitBreaker(name = "notificationService", fallbackMethod = "sendPaymentReminderEmailFallback")
    public void sendPaymentReminderEmail(PaymentReminderEmailRequest request) {
        log.info("Sending payment reminder email to: {}", request.getDebtorEmail());
        String url = NOTIFICATION_SERVICE_URL + "/payment-reminder";
        restTemplate.postForObject(url, request, Object.class);
        log.info("Payment reminder email sent successfully");
    }

    public void sendPaymentReminderEmailFallback(PaymentReminderEmailRequest request, Throwable t) {
        log.warn("Circuit breaker fallback for sendPaymentReminderEmail: {}", t.getMessage());
    }
}
