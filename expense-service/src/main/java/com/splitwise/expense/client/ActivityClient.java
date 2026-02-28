package com.splitwise.expense.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class ActivityClient {

    private final RestTemplate restTemplate;
    private final String activityServiceUrl;

    public ActivityClient(
            RestTemplate restTemplate,
            @Value("${activity.service.url:http://localhost:8085}") String activityServiceUrl) {
        this.restTemplate = restTemplate;
        this.activityServiceUrl = activityServiceUrl;
    }

    /**
     * Log an activity to the activity service (async - non-blocking)
     */
    @Async
    public void logActivity(ActivityRequest request) {
        try {
            log.info("Logging activity: {} for group: {}", request.getActivityType(), request.getGroupId());
            restTemplate.postForObject(
                    activityServiceUrl + "/api/activities",
                    request,
                    String.class);
            log.info("Activity logged successfully for group: {}", request.getGroupId());
        } catch (Exception e) {
            // Don't fail the main operation if activity logging fails
            log.warn("Failed to log activity (non-blocking): {}", e.getMessage());
        }
    }
}
