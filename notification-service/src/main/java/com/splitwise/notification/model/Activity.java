package com.splitwise.notification.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "activities", indexes = {
        @Index(name = "idx_group_id", columnList = "groupId"),
        @Index(name = "idx_user_id", columnList = "userId"),
        @Index(name = "idx_timestamp", columnList = "timestamp")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Activity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ActivityType activityType;

    @Column(nullable = false)
    private String userId; // User who performed the action

    @Column
    private String userName; // Name of user who performed the action

    @Column(nullable = false)
    private Long groupId; // Group where activity occurred

    @Column
    private String groupName; // Name of the group

    @Column(nullable = false, length = 500)
    private String description; // Human-readable description

    @Column(columnDefinition = "TEXT")
    private String metadata; // JSON string with additional data

    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();

    // Optional: for notifications
    @Column
    private String targetUserId; // User affected by the action (e.g., added to group)

    @Column
    private String targetUserName; // Name of user affected by the action
}
