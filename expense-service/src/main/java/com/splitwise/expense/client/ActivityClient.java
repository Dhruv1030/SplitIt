package com.splitwise.expense.client;

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
            @Value("${activity.service.url:http://localhost:8085}") String activityServiceUrl) {
        this.restTemplate = restTemplate;
        this.activityServiceUrl = activityServiceUrl;
    }

    /**
     * Log an activity to the activity service (async - non-blocking)
     */
    @Async
    @CircuitBreaker(name = "notificationService", fallbackMethod = "logActivityFallback")
    public void logActivity(ActivityRequest request) {
        log.info("Logging activity: {} for group: {}", request.getActivityType(), request.getGroupId());
        restTemplate.postForObject(
                activityServiceUrl + "/api/activities",
                request,
                String.class);
        log.info("Activity logged successfully for group: {}", request.getGroupId());
    }

    public void logActivityFallback(ActivityRequest request, Throwable t) {
        log.warn("Circuit breaker fallback for logActivity({}): {}", request.getActivityType(), t.getMessage());
    }
}
