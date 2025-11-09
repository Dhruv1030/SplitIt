package com.splitwise.notification.controller;

import com.splitwise.notification.dto.ActivityResponse;
import com.splitwise.notification.dto.CreateActivityRequest;
import com.splitwise.notification.service.ActivityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/activities")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Activity Feed", description = "APIs for activity logging and feed management across groups and users")
public class ActivityController {

    private final ActivityService activityService;

    @Operation(summary = "Create activity log", description = "Log a new activity (used by other microservices to track events)")
    @PostMapping
    public ResponseEntity<ActivityResponse> createActivity(
            @Valid @RequestBody CreateActivityRequest request) {

        log.info("Creating activity: {} for group: {}", request.getActivityType(), request.getGroupId());
        ActivityResponse response = activityService.logActivity(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Get group activities (paginated)", description = "Retrieve paginated activity feed for a specific group")
    @GetMapping("/group/{groupId}")
    public ResponseEntity<Page<ActivityResponse>> getGroupActivities(
            @PathVariable Long groupId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.info("Fetching activities for group: {}, page: {}, size: {}", groupId, page, size);
        Page<ActivityResponse> activities = activityService.getGroupActivities(groupId, page, size);
        return ResponseEntity.ok(activities);
    }

    @Operation(summary = "Get recent group activities", description = "Retrieve the 10 most recent activities for a group")
    @GetMapping("/group/{groupId}/recent")
    public ResponseEntity<List<ActivityResponse>> getRecentGroupActivities(
            @PathVariable Long groupId) {

        log.info("Fetching recent activities for group: {}", groupId);
        List<ActivityResponse> activities = activityService.getRecentGroupActivities(groupId);
        return ResponseEntity.ok(activities);
    }

    @Operation(summary = "Get user activities (paginated)", description = "Retrieve paginated activity feed for a specific user across all groups")
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<ActivityResponse>> getUserActivities(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.info("Fetching activities for user: {}, page: {}, size: {}", userId, page, size);
        Page<ActivityResponse> activities = activityService.getUserActivities(userId, page, size);
        return ResponseEntity.ok(activities);
    }

    @Operation(summary = "Get activities by date range", description = "Retrieve group activities within a specific date range")
    @GetMapping("/group/{groupId}/range")
    public ResponseEntity<List<ActivityResponse>> getGroupActivitiesByDateRange(
            @PathVariable Long groupId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        log.info("Fetching activities for group: {} from {} to {}", groupId, startDate, endDate);
        List<ActivityResponse> activities = activityService.getGroupActivitiesByDateRange(
                groupId, startDate, endDate);
        return ResponseEntity.ok(activities);
    }

    @Operation(summary = "Get activity count", description = "Get total count of activities for a group")
    @GetMapping("/group/{groupId}/count")
    public ResponseEntity<Long> getGroupActivityCount(@PathVariable Long groupId) {
        log.info("Fetching activity count for group: {}", groupId);
        long count = activityService.getGroupActivityCount(groupId);
        return ResponseEntity.ok(count);
    }

    @Operation(summary = "Health check", description = "Check if the activity service is running")
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Activity Service is running!");
    }
}
