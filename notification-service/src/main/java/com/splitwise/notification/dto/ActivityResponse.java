package com.splitwise.notification.dto;

import com.splitwise.notification.model.ActivityType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityResponse {
    private Long id;
    private ActivityType activityType;
    private String userId;
    private String userName; // Enriched from User Service
    private Long groupId;
    private String groupName; // Enriched from Group Service
    private String description;
    private String metadata;
    private String targetUserId;
    private String targetUserName; // Enriched from User Service
    private LocalDateTime timestamp;
}
