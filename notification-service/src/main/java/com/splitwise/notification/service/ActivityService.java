package com.splitwise.notification.service;

import com.splitwise.notification.dto.ActivityResponse;
import com.splitwise.notification.dto.CreateActivityRequest;
import com.splitwise.notification.model.Activity;
import com.splitwise.notification.repository.ActivityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActivityService {

    private final ActivityRepository activityRepository;
    private final RestTemplate restTemplate;

    /**
     * Log a new activity
     */
    @Transactional
    public ActivityResponse logActivity(CreateActivityRequest request) {
        log.info("Logging activity: {} for group: {} by user: {}",
                request.getActivityType(), request.getGroupId(), request.getUserId());

        // Fetch user name and group name
        String userName = getUserName(request.getUserId());
        String groupName = getGroupName(request.getGroupId(), request.getUserId());
        String targetUserName = request.getTargetUserId() != null ? getUserName(request.getTargetUserId()) : null;

        // Build description with user names instead of IDs
        String description = buildDescription(request, userName, targetUserName);

        Activity activity = Activity.builder()
                .activityType(request.getActivityType())
                .userId(request.getUserId())
                .userName(userName)
                .groupId(request.getGroupId())
                .groupName(groupName)
                .description(description)
                .metadata(request.getMetadata())
                .targetUserId(request.getTargetUserId())
                .targetUserName(targetUserName)
                .timestamp(LocalDateTime.now())
                .build();

        activity = activityRepository.save(activity);
        log.info("Activity logged with ID: {}", activity.getId());

        return toResponse(activity);
    }

    /**
     * Get activities for a group (paginated)
     */
    @Transactional(readOnly = true)
    public Page<ActivityResponse> getGroupActivities(Long groupId, int page, int size) {
        log.info("Fetching activities for group: {}, page: {}, size: {}", groupId, page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<Activity> activities = activityRepository.findByGroupIdOrderByTimestampDesc(groupId, pageable);

        return activities.map(this::toResponse);
    }

    /**
     * Get recent activities for a group (last 10)
     */
    @Transactional(readOnly = true)
    public List<ActivityResponse> getRecentGroupActivities(Long groupId) {
        log.info("Fetching recent activities for group: {}", groupId);

        List<Activity> activities = activityRepository.findTop10ByGroupIdOrderByTimestampDesc(groupId);

        return activities.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get activities for a user (paginated)
     */
    @Transactional(readOnly = true)
    public Page<ActivityResponse> getUserActivities(String userId, int page, int size) {
        log.info("Fetching activities for user: {}, page: {}, size: {}", userId, page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<Activity> activities = activityRepository.findByUserIdOrderByTimestampDesc(userId, pageable);

        return activities.map(this::toResponse);
    }

    /**
     * Get activities for a group within date range
     */
    @Transactional(readOnly = true)
    public List<ActivityResponse> getGroupActivitiesByDateRange(
            Long groupId, LocalDateTime startDate, LocalDateTime endDate) {

        log.info("Fetching activities for group: {} from {} to {}", groupId, startDate, endDate);

        List<Activity> activities = activityRepository
                .findByGroupIdAndTimestampBetweenOrderByTimestampDesc(groupId, startDate, endDate);

        return activities.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get activity count for a group
     */
    @Transactional(readOnly = true)
    public long getGroupActivityCount(Long groupId) {
        return activityRepository.countByGroupId(groupId);
    }

    /**
     * Convert Activity entity to Response DTO
     */
    private ActivityResponse toResponse(Activity activity) {
        return ActivityResponse.builder()
                .id(activity.getId())
                .activityType(activity.getActivityType())
                .userId(activity.getUserId())
                .userName(activity.getUserName())
                .groupId(activity.getGroupId())
                .groupName(activity.getGroupName())
                .description(activity.getDescription())
                .metadata(activity.getMetadata())
                .targetUserId(activity.getTargetUserId())
                .targetUserName(activity.getTargetUserName())
                .timestamp(activity.getTimestamp())
                .build();
    }

    /**
     * Fetch user name from user-service
     */
    private String getUserName(String userId) {
        try {
            String url = "http://user-service:8081/api/users/" + userId;
            log.debug("Fetching user name from: {}", url);

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response != null && response.containsKey("name")) {
                return (String) response.get("name");
            }

            log.warn("User name not found for userId: {}", userId);
            return "Unknown User";
        } catch (Exception e) {
            log.error("Failed to fetch user name for userId: {}", userId, e);
            return "Unknown User";
        }
    }

    /**
     * Fetch group name from group-service
     */
    private String getGroupName(Long groupId, String userId) {
        try {
            String url = "http://group-service:8082/api/groups/" + groupId;
            log.debug("Fetching group name from: {}", url);

            // Group service requires X-User-Id header
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("X-User-Id", userId);

            org.springframework.http.HttpEntity<?> entity = new org.springframework.http.HttpEntity<>(headers);

            @SuppressWarnings("unchecked")
            org.springframework.http.ResponseEntity<Map<String, Object>> responseEntity = restTemplate.exchange(url,
                    org.springframework.http.HttpMethod.GET, entity,
                    (Class<Map<String, Object>>) (Class<?>) Map.class);

            Map<String, Object> response = responseEntity.getBody();

            if (response != null && response.containsKey("data")) {
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) response.get("data");
                if (data != null && data.containsKey("name")) {
                    return (String) data.get("name");
                }
            }

            log.warn("Group name not found for groupId: {}", groupId);
            return "Unknown Group";
        } catch (Exception e) {
            log.error("Failed to fetch group name for groupId: {}", groupId, e);
            return "Unknown Group";
        }
    }

    /**
     * Build human-readable description using user names
     */
    private String buildDescription(CreateActivityRequest request, String userName, String targetUserName) {
        // If description is provided in request, use it
        if (request.getDescription() != null && !request.getDescription().isEmpty()) {
            // Replace user IDs with names if present
            String description = request.getDescription();
            description = description.replace(request.getUserId(), userName);
            if (request.getTargetUserId() != null && targetUserName != null) {
                description = description.replace(request.getTargetUserId(), targetUserName);
            }
            return description;
        }

        // Otherwise, build description based on activity type
        switch (request.getActivityType()) {
            case GROUP_CREATED:
                return userName + " created the group";

            case MEMBER_ADDED:
                return userName + " added " + (targetUserName != null ? targetUserName : "a member") + " to the group";

            case MEMBER_REMOVED:
                return userName + " removed " + (targetUserName != null ? targetUserName : "a member")
                        + " from the group";

            case EXPENSE_ADDED:
                return userName + " added an expense";

            case EXPENSE_UPDATED:
                return userName + " updated an expense";

            case EXPENSE_DELETED:
                return userName + " deleted an expense";

            case SETTLEMENT_RECORDED:
                return userName + " recorded a settlement";

            case PAYMENT_COMPLETED:
                return userName + " completed a payment";

            default:
                return userName + " performed an action";
        }
    }
}
