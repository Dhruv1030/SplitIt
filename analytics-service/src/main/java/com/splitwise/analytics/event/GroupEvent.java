package com.splitwise.analytics.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupEvent {
    private String eventType;
    private Long groupId;
    private String groupName;
    private String userId;
    private String targetUserId;
    private LocalDateTime timestamp;
}
