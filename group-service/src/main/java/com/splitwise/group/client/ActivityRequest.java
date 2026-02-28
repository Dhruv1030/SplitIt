package com.splitwise.group.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityRequest {
    private String activityType;
    private String userId;
    private Long groupId;
    private String description;
    private String metadata;
    private String targetUserId;
}
