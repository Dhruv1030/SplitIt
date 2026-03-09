package com.splitwise.group.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
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
            @Value("${activity.service.url:http://notification-service:8085}") String activityServiceUrl) {
        this.restTemplate = restTemplate;
        this.activityServiceUrl = activityServiceUrl;
    }

    /**
     * Log an activity to the notification service (async - non-blocking)
     */
    @Async
    @CircuitBreaker(name = "notificationService", fallbackMethod = "logActivityFallback")
    public void logActivity(ActivityRequest request) {
        log.debug("Logging activity: {} for group: {}", request.getActivityType(), request.getGroupId());
        restTemplate.postForEntity(activityServiceUrl + "/api/activities", request, Object.class);
        log.debug("Activity logged successfully");
    }

    public void logActivityFallback(ActivityRequest request, Throwable t) {
        log.warn("Circuit breaker fallback for logActivity({}): {}", request.getActivityType(), t.getMessage());
    }
}
