package com.splitwise.group.controller;

import com.splitwise.group.dto.*;
import com.splitwise.group.service.GroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
@Slf4j
public class GroupController {

    private final GroupService groupService;

    @PostMapping
    public ResponseEntity<ApiResponse<GroupResponse>> createGroup(
            @Valid @RequestBody CreateGroupRequest request,
            @RequestHeader("X-User-Id") String userId) {

        log.info("Received request to create group: {} by user: {}", request.getName(), userId);
        GroupResponse group = groupService.createGroup(request, userId);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Group created successfully", group));
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<ApiResponse<GroupResponse>> getGroupById(
            @PathVariable Long groupId,
            @RequestHeader("X-User-Id") String userId) {

        log.info("Received request to get group: {} by user: {}", groupId, userId);
        GroupResponse group = groupService.getGroupById(groupId, userId);
        return ResponseEntity.ok(ApiResponse.success(group));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<GroupResponse>>> getUserGroups(
            @RequestHeader("X-User-Id") String userId) {

        log.info("Received request to get all groups for user: {}", userId);
        List<GroupResponse> groups = groupService.getUserGroups(userId);
        return ResponseEntity.ok(ApiResponse.success(groups));
    }

    @GetMapping("/created")
    public ResponseEntity<ApiResponse<List<GroupResponse>>> getGroupsCreatedByUser(
            @RequestHeader("X-User-Id") String userId) {

        log.info("Received request to get groups created by user: {}", userId);
        List<GroupResponse> groups = groupService.getGroupsCreatedByUser(userId);
        return ResponseEntity.ok(ApiResponse.success(groups));
    }

    @PutMapping("/{groupId}")
    public ResponseEntity<ApiResponse<GroupResponse>> updateGroup(
            @PathVariable Long groupId,
            @Valid @RequestBody UpdateGroupRequest request,
            @RequestHeader("X-User-Id") String userId) {

        log.info("Received request to update group: {} by user: {}", groupId, userId);
        GroupResponse group = groupService.updateGroup(groupId, request, userId);
        return ResponseEntity.ok(ApiResponse.success("Group updated successfully", group));
    }

    @DeleteMapping("/{groupId}")
    public ResponseEntity<ApiResponse<Void>> deleteGroup(
            @PathVariable Long groupId,
            @RequestHeader("X-User-Id") String userId) {

        log.info("Received request to delete group: {} by user: {}", groupId, userId);
        groupService.deleteGroup(groupId, userId);
        return ResponseEntity.ok(ApiResponse.success("Group deleted successfully", null));
    }

    @PostMapping("/{groupId}/members")
    public ResponseEntity<ApiResponse<GroupResponse>> addMember(
            @PathVariable Long groupId,
            @Valid @RequestBody AddMemberRequest request,
            @RequestHeader("X-User-Id") String userId) {

        log.info("Received request to add member: {} to group: {} by user: {}",
                request.getUserId(), groupId, userId);
        GroupResponse group = groupService.addMember(groupId, request, userId);
        return ResponseEntity.ok(ApiResponse.success("Member added successfully", group));
    }

    @DeleteMapping("/{groupId}/members/{memberId}")
    public ResponseEntity<ApiResponse<GroupResponse>> removeMember(
            @PathVariable Long groupId,
            @PathVariable String memberId,
            @RequestHeader("X-User-Id") String userId) {

        log.info("Received request to remove member: {} from group: {} by user: {}",
                memberId, groupId, userId);
        GroupResponse group = groupService.removeMember(groupId, memberId, userId);
        return ResponseEntity.ok(ApiResponse.success("Member removed successfully", group));
    }

    @PatchMapping("/{groupId}/members/{memberId}/role")
    public ResponseEntity<ApiResponse<GroupResponse>> updateMemberRole(
            @PathVariable Long groupId,
            @PathVariable String memberId,
            @RequestParam String role,
            @RequestHeader("X-User-Id") String userId) {

        log.info("Received request to update role for member: {} in group: {} to: {} by user: {}",
                memberId, groupId, role, userId);
        GroupResponse group = groupService.updateMemberRole(groupId, memberId, role, userId);
        return ResponseEntity.ok(ApiResponse.success("Member role updated successfully", group));
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Group Service is running!");
    }
}
