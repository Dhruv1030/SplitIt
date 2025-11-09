package com.splitwise.group.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
@Slf4j
public class ActivityClient {

    private final RestTemplate restTemplate;
    private static final String ACTIVITY_SERVICE_URL = "http://notification-service:8085/api/activities";

    /**
     * Log an activity to the notification service
     * Non-blocking - failures won't affect the main operation
     */
    public void logActivity(ActivityRequest request) {
        try {
            log.debug("Logging activity: {} for group: {}", request.getActivityType(), request.getGroupId());
            restTemplate.postForEntity(ACTIVITY_SERVICE_URL, request, Object.class);
            log.debug("Activity logged successfully");
        } catch (Exception e) {
            // Log but don't throw - activity logging should never break main functionality
            log.error("Failed to log activity: {}", e.getMessage());
        }
    }
}
