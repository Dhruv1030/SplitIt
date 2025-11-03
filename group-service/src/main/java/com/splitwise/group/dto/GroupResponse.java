package com.splitwise.group.dto;

import com.splitwise.group.model.GroupMember;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupResponse {

    private Long id;
    private String name;
    private String description;
    private String category;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isActive;
    private List<MemberResponse> members;
    private int memberCount;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MemberResponse {
        private Long id;
        private String userId;
        private String name; // User's display name
        private String email; // User's email
        private GroupMember.MemberRole role;
        private LocalDateTime joinedAt;
    }
}
