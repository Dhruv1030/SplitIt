package com.splitwise.notification.dto;

import com.splitwise.notification.model.ActivityType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateActivityRequest {

    @NotNull(message = "Activity type is required")
    private ActivityType activityType;

    @NotBlank(message = "User ID is required")
    private String userId;

    @NotNull(message = "Group ID is required")
    private Long groupId;

    @NotBlank(message = "Description is required")
    private String description;

    private String metadata; // JSON string

    private String targetUserId; // Optional: user affected by the action
}
