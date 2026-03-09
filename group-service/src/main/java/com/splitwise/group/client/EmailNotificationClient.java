package com.splitwise.group.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailNotificationClient {

    private final RestTemplate restTemplate;
    private static final String NOTIFICATION_SERVICE_URL = "http://notification-service:8085/api/notifications";

    /**
     * Send group invitation email
     */
    @CircuitBreaker(name = "notificationService", fallbackMethod = "sendGroupInvitationEmailFallback")
    public void sendGroupInvitationEmail(GroupInvitationEmailRequest request) {
        log.info("Sending group invitation email to: {}", request.getInviteeEmail());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<GroupInvitationEmailRequest> entity = new HttpEntity<>(request, headers);

        restTemplate.postForEntity(
                NOTIFICATION_SERVICE_URL + "/group-invitation",
                entity,
                String.class);

        log.info("Group invitation email sent successfully to: {}", request.getInviteeEmail());
    }

    public void sendGroupInvitationEmailFallback(GroupInvitationEmailRequest request, Throwable t) {
        log.warn("Circuit breaker fallback for sendGroupInvitationEmail: {}", t.getMessage());
    }
}
