package com.splitwise.group.service;

import com.splitwise.group.dto.*;
import com.splitwise.group.exception.BadRequestException;
import com.splitwise.group.exception.ResourceNotFoundException;
import com.splitwise.group.exception.UnauthorizedException;
import com.splitwise.group.model.Group;
import com.splitwise.group.model.GroupMember;
import com.splitwise.group.repository.GroupMemberRepository;
import com.splitwise.group.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GroupService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;

    @Transactional
    public GroupResponse createGroup(CreateGroupRequest request, String userId) {
        log.info("Creating group: {} by user: {}", request.getName(), userId);

        // Create the group
        Group group = Group.builder()
                .name(request.getName())
                .description(request.getDescription())
                .category(request.getCategory())
                .createdBy(userId)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .isActive(true)
                .members(new ArrayList<>())
                .build();

        // Add creator as ADMIN
        GroupMember creator = GroupMember.builder()
                .userId(userId)
                .role(GroupMember.MemberRole.ADMIN)
                .joinedAt(LocalDateTime.now())
                .group(group)
                .build();

        group.getMembers().add(creator);

        // Add additional members if provided
        if (request.getMemberIds() != null && !request.getMemberIds().isEmpty()) {
            for (String memberId : request.getMemberIds()) {
                if (!memberId.equals(userId)) { // Avoid duplicate creator
                    GroupMember member = GroupMember.builder()
                            .userId(memberId)
                            .role(GroupMember.MemberRole.MEMBER)
                            .joinedAt(LocalDateTime.now())
                            .group(group)
                            .build();
                    group.getMembers().add(member);
                }
            }
        }

        Group savedGroup = groupRepository.save(group);
        log.info("Group created successfully with ID: {}", savedGroup.getId());

        return mapToGroupResponse(savedGroup);
    }

    @Transactional(readOnly = true)
    public GroupResponse getGroupById(Long groupId, String userId) {
        log.info("Fetching group: {} for user: {}", groupId, userId);

        Group group = groupRepository.findByIdAndIsActiveTrue(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + groupId));

        // Check if user is a member of the group
        if (!isUserMemberOfGroup(groupId, userId)) {
            throw new UnauthorizedException("You are not a member of this group");
        }

        return mapToGroupResponse(group);
    }

    @Transactional(readOnly = true)
    public List<GroupResponse> getUserGroups(String userId) {
        log.info("Fetching all groups for user: {}", userId);

        List<Group> groups = groupRepository.findGroupsByUserId(userId);
        return groups.stream()
                .map(this::mapToGroupResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<GroupResponse> getGroupsCreatedByUser(String userId) {
        log.info("Fetching groups created by user: {}", userId);

        List<Group> groups = groupRepository.findGroupsCreatedByUser(userId);
        return groups.stream()
                .map(this::mapToGroupResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public GroupResponse updateGroup(Long groupId, UpdateGroupRequest request, String userId) {
        log.info("Updating group: {} by user: {}", groupId, userId);

        Group group = groupRepository.findByIdAndIsActiveTrue(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + groupId));

        // Check if user is admin of the group
        if (!isUserAdminOfGroup(groupId, userId)) {
            throw new UnauthorizedException("Only group admins can update group details");
        }

        // Update fields if provided
        if (request.getName() != null && !request.getName().isBlank()) {
            group.setName(request.getName());
        }
        if (request.getDescription() != null) {
            group.setDescription(request.getDescription());
        }
        if (request.getCategory() != null && !request.getCategory().isBlank()) {
            group.setCategory(request.getCategory());
        }

        group.setUpdatedAt(LocalDateTime.now());

        Group updatedGroup = groupRepository.save(group);
        log.info("Group updated successfully: {}", groupId);

        return mapToGroupResponse(updatedGroup);
    }

    @Transactional
    public void deleteGroup(Long groupId, String userId) {
        log.info("Deleting group: {} by user: {}", groupId, userId);

        Group group = groupRepository.findByIdAndIsActiveTrue(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + groupId));

        // Only the creator can delete the group
        if (!group.getCreatedBy().equals(userId)) {
            throw new UnauthorizedException("Only the group creator can delete the group");
        }

        // Soft delete
        group.setActive(false);
        group.setUpdatedAt(LocalDateTime.now());
        groupRepository.save(group);

        log.info("Group deleted successfully: {}", groupId);
    }

    @Transactional
    public GroupResponse addMember(Long groupId, AddMemberRequest request, String requesterId) {
        log.info("Adding member: {} to group: {} by user: {}", request.getUserId(), groupId, requesterId);

        Group group = groupRepository.findByIdAndIsActiveTrue(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + groupId));

        // Check if requester is admin
        if (!isUserAdminOfGroup(groupId, requesterId)) {
            throw new UnauthorizedException("Only group admins can add members");
        }

        // Check if user is already a member
        if (groupMemberRepository.existsByGroupIdAndUserId(groupId, request.getUserId())) {
            throw new BadRequestException("User is already a member of this group");
        }

        // Determine role
        GroupMember.MemberRole role = GroupMember.MemberRole.MEMBER;
        if (request.getRole() != null && request.getRole().equalsIgnoreCase("ADMIN")) {
            role = GroupMember.MemberRole.ADMIN;
        }

        // Add new member
        GroupMember newMember = GroupMember.builder()
                .userId(request.getUserId())
                .role(role)
                .joinedAt(LocalDateTime.now())
                .group(group)
                .build();

        group.getMembers().add(newMember);
        group.setUpdatedAt(LocalDateTime.now());

        Group updatedGroup = groupRepository.save(group);
        log.info("Member added successfully to group: {}", groupId);

        return mapToGroupResponse(updatedGroup);
    }

    @Transactional
    public GroupResponse removeMember(Long groupId, String userIdToRemove, String requesterId) {
        log.info("Removing member: {} from group: {} by user: {}", userIdToRemove, groupId, requesterId);

        Group group = groupRepository.findByIdAndIsActiveTrue(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + groupId));

        // Users can remove themselves, or admins can remove others
        boolean isSelfRemoval = userIdToRemove.equals(requesterId);
        boolean isAdminRemoval = isUserAdminOfGroup(groupId, requesterId);

        if (!isSelfRemoval && !isAdminRemoval) {
            throw new UnauthorizedException("You don't have permission to remove this member");
        }

        // Check if member exists
        GroupMember memberToRemove = groupMemberRepository.findByGroupIdAndUserId(groupId, userIdToRemove)
                .orElseThrow(() -> new ResourceNotFoundException("User is not a member of this group"));

        // Prevent removing the last admin
        if (memberToRemove.getRole() == GroupMember.MemberRole.ADMIN) {
            int adminCount = groupMemberRepository.countAdminsByGroupId(groupId);
            if (adminCount <= 1) {
                throw new BadRequestException("Cannot remove the last admin from the group");
            }
        }

        // Remove member
        group.getMembers().remove(memberToRemove);
        groupMemberRepository.delete(memberToRemove);
        group.setUpdatedAt(LocalDateTime.now());

        Group updatedGroup = groupRepository.save(group);
        log.info("Member removed successfully from group: {}", groupId);

        return mapToGroupResponse(updatedGroup);
    }

    @Transactional
    public GroupResponse updateMemberRole(Long groupId, String userId, String newRole, String requesterId) {
        log.info("Updating role for member: {} in group: {} to role: {} by user: {}",
                userId, groupId, newRole, requesterId);

        Group group = groupRepository.findByIdAndIsActiveTrue(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + groupId));

        // Check if requester is admin
        if (!isUserAdminOfGroup(groupId, requesterId)) {
            throw new UnauthorizedException("Only group admins can update member roles");
        }

        // Find member
        GroupMember member = groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("User is not a member of this group"));

        // Parse new role
        GroupMember.MemberRole role;
        try {
            role = GroupMember.MemberRole.valueOf(newRole.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid role: " + newRole);
        }

        // If demoting from admin to member, ensure there's at least one admin left
        if (member.getRole() == GroupMember.MemberRole.ADMIN && role == GroupMember.MemberRole.MEMBER) {
            int adminCount = groupMemberRepository.countAdminsByGroupId(groupId);
            if (adminCount <= 1) {
                throw new BadRequestException("Cannot demote the last admin");
            }
        }

        member.setRole(role);
        groupMemberRepository.save(member);
        group.setUpdatedAt(LocalDateTime.now());
        groupRepository.save(group);

        log.info("Member role updated successfully in group: {}", groupId);

        return mapToGroupResponse(group);
    }

    private boolean isUserMemberOfGroup(Long groupId, String userId) {
        return groupMemberRepository.existsByGroupIdAndUserId(groupId, userId);
    }

    private boolean isUserAdminOfGroup(Long groupId, String userId) {
        return groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
                .map(member -> member.getRole() == GroupMember.MemberRole.ADMIN)
                .orElse(false);
    }

    private GroupResponse mapToGroupResponse(Group group) {
        List<GroupResponse.MemberResponse> members = group.getMembers().stream()
                .map(member -> GroupResponse.MemberResponse.builder()
                        .id(member.getId())
                        .userId(member.getUserId())
                        .role(member.getRole())
                        .joinedAt(member.getJoinedAt())
                        .build())
                .collect(Collectors.toList());

        return GroupResponse.builder()
                .id(group.getId())
                .name(group.getName())
                .description(group.getDescription())
                .category(group.getCategory())
                .createdBy(group.getCreatedBy())
                .createdAt(group.getCreatedAt())
                .updatedAt(group.getUpdatedAt())
                .isActive(group.isActive())
                .members(members)
                .memberCount(members.size())
                .build();
    }
}
