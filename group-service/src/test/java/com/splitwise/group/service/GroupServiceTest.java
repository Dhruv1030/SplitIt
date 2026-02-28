package com.splitwise.group.service;

import com.splitwise.group.dto.AddMemberRequest;
import com.splitwise.group.dto.CreateGroupRequest;
import com.splitwise.group.dto.GroupResponse;
import com.splitwise.group.dto.UpdateGroupRequest;
import com.splitwise.group.client.ActivityClient;
import com.splitwise.group.client.EmailNotificationClient;
import com.splitwise.group.client.UserClient;
import com.splitwise.group.exception.BadRequestException;
import com.splitwise.group.exception.ResourceNotFoundException;
import com.splitwise.group.exception.UnauthorizedException;
import com.splitwise.group.model.Group;
import com.splitwise.group.model.GroupMember;
import com.splitwise.group.repository.GroupMemberRepository;
import com.splitwise.group.repository.GroupRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GroupServiceTest {

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private GroupMemberRepository groupMemberRepository;

    @Mock
    private UserClient userClient;

    @Mock
    private ActivityClient activityClient;

    @Mock
    private EmailNotificationClient emailNotificationClient;

    @InjectMocks
    private GroupService groupService;

    private Group testGroup;
    private GroupMember testMember;
    private String testUserId = "user123";
    private String testUserId2 = "user456";

    @BeforeEach
    void setUp() {
        testMember = GroupMember.builder()
                .id(1L)
                .userId(testUserId)
                .role(GroupMember.MemberRole.ADMIN)
                .joinedAt(LocalDateTime.now())
                .build();

        testGroup = Group.builder()
                .id(1L)
                .name("Test Group")
                .description("Test Description")
                .category("TRIP")
                .createdBy(testUserId)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .isActive(true)
                .members(new ArrayList<>(Arrays.asList(testMember)))
                .build();

        testMember.setGroup(testGroup);

        // Mock UserClient to return user details when mapping group responses
        UserClient.UserDTO mockUser = new UserClient.UserDTO();
        mockUser.setId(testUserId);
        mockUser.setName("Test User");
        mockUser.setEmail("test@example.com");
        lenient().when(userClient.getUserById(anyString())).thenReturn(mockUser);
    }

    @Test
    void createGroup_Success() {
        // Arrange
        CreateGroupRequest request = CreateGroupRequest.builder()
                .name("Trip to Paris")
                .description("Europe vacation")
                .category("TRIP")
                .memberIds(Arrays.asList(testUserId2))
                .build();

        when(groupRepository.save(any(Group.class))).thenReturn(testGroup);

        // Act
        GroupResponse response = groupService.createGroup(request, testUserId);

        // Assert
        assertNotNull(response);
        assertEquals("Test Group", response.getName());
        verify(groupRepository, times(1)).save(any(Group.class));
    }

    @Test
    void getGroupById_Success() {
        // Arrange
        when(groupRepository.findByIdAndIsActiveTrue(1L)).thenReturn(Optional.of(testGroup));
        when(groupMemberRepository.existsByGroupIdAndUserId(1L, testUserId)).thenReturn(true);

        // Act
        GroupResponse response = groupService.getGroupById(1L, testUserId);

        // Assert
        assertNotNull(response);
        assertEquals("Test Group", response.getName());
        assertEquals(testUserId, response.getCreatedBy());
    }

    @Test
    void getGroupById_NotFound() {
        // Arrange
        when(groupRepository.findByIdAndIsActiveTrue(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            groupService.getGroupById(999L, testUserId);
        });
    }

    @Test
    void getGroupById_Unauthorized() {
        // Arrange
        when(groupRepository.findByIdAndIsActiveTrue(1L)).thenReturn(Optional.of(testGroup));
        when(groupMemberRepository.existsByGroupIdAndUserId(1L, "unauthorized")).thenReturn(false);

        // Act & Assert
        assertThrows(UnauthorizedException.class, () -> {
            groupService.getGroupById(1L, "unauthorized");
        });
    }

    @Test
    void getUserGroups_Success() {
        // Arrange
        List<Group> groups = Arrays.asList(testGroup);
        when(groupRepository.findGroupsByUserId(testUserId)).thenReturn(groups);

        // Act
        List<GroupResponse> responses = groupService.getUserGroups(testUserId);

        // Assert
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("Test Group", responses.get(0).getName());
    }

    @Test
    void updateGroup_Success() {
        // Arrange
        UpdateGroupRequest request = UpdateGroupRequest.builder()
                .name("Updated Group Name")
                .description("Updated Description")
                .build();

        when(groupRepository.findByIdAndIsActiveTrue(1L)).thenReturn(Optional.of(testGroup));
        when(groupMemberRepository.findByGroupIdAndUserId(1L, testUserId))
                .thenReturn(Optional.of(testMember));
        when(groupRepository.save(any(Group.class))).thenReturn(testGroup);

        // Act
        GroupResponse response = groupService.updateGroup(1L, request, testUserId);

        // Assert
        assertNotNull(response);
        verify(groupRepository, times(1)).save(any(Group.class));
    }

    @Test
    void updateGroup_Unauthorized() {
        // Arrange
        UpdateGroupRequest request = UpdateGroupRequest.builder()
                .name("Updated Name")
                .build();

        GroupMember regularMember = GroupMember.builder()
                .userId(testUserId2)
                .role(GroupMember.MemberRole.MEMBER)
                .build();

        when(groupRepository.findByIdAndIsActiveTrue(1L)).thenReturn(Optional.of(testGroup));
        when(groupMemberRepository.findByGroupIdAndUserId(1L, testUserId2))
                .thenReturn(Optional.of(regularMember));

        // Act & Assert
        assertThrows(UnauthorizedException.class, () -> {
            groupService.updateGroup(1L, request, testUserId2);
        });
    }

    @Test
    void deleteGroup_Success() {
        // Arrange
        when(groupRepository.findByIdAndIsActiveTrue(1L)).thenReturn(Optional.of(testGroup));
        when(groupRepository.save(any(Group.class))).thenReturn(testGroup);

        // Act
        groupService.deleteGroup(1L, testUserId);

        // Assert
        verify(groupRepository, times(1)).save(any(Group.class));
    }

    @Test
    void deleteGroup_Unauthorized() {
        // Arrange
        when(groupRepository.findByIdAndIsActiveTrue(1L)).thenReturn(Optional.of(testGroup));

        // Act & Assert
        assertThrows(UnauthorizedException.class, () -> {
            groupService.deleteGroup(1L, testUserId2);
        });
    }

    @Test
    void addMember_Success() {
        // Arrange
        AddMemberRequest request = AddMemberRequest.builder()
                .userId(testUserId2)
                .role("MEMBER")
                .build();

        when(groupRepository.findByIdAndIsActiveTrue(1L)).thenReturn(Optional.of(testGroup));
        when(groupMemberRepository.findByGroupIdAndUserId(1L, testUserId))
                .thenReturn(Optional.of(testMember));
        when(groupMemberRepository.existsByGroupIdAndUserId(1L, testUserId2)).thenReturn(false);
        when(groupRepository.save(any(Group.class))).thenReturn(testGroup);

        // Act
        GroupResponse response = groupService.addMember(1L, request, testUserId);

        // Assert
        assertNotNull(response);
        verify(groupRepository, times(1)).save(any(Group.class));
    }

    @Test
    void addMember_AlreadyExists() {
        // Arrange
        AddMemberRequest request = AddMemberRequest.builder()
                .userId(testUserId2)
                .build();

        when(groupRepository.findByIdAndIsActiveTrue(1L)).thenReturn(Optional.of(testGroup));
        when(groupMemberRepository.findByGroupIdAndUserId(1L, testUserId))
                .thenReturn(Optional.of(testMember));
        when(groupMemberRepository.existsByGroupIdAndUserId(1L, testUserId2)).thenReturn(true);

        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            groupService.addMember(1L, request, testUserId);
        });
    }

    @Test
    void removeMember_Success() {
        // Arrange
        GroupMember memberToRemove = GroupMember.builder()
                .id(2L)
                .userId(testUserId2)
                .role(GroupMember.MemberRole.MEMBER)
                .group(testGroup)
                .build();

        when(groupRepository.findByIdAndIsActiveTrue(1L)).thenReturn(Optional.of(testGroup));
        when(groupMemberRepository.findByGroupIdAndUserId(1L, testUserId))
                .thenReturn(Optional.of(testMember));
        when(groupMemberRepository.findByGroupIdAndUserId(1L, testUserId2))
                .thenReturn(Optional.of(memberToRemove));
        when(groupRepository.save(any(Group.class))).thenReturn(testGroup);

        // Act
        GroupResponse response = groupService.removeMember(1L, testUserId2, testUserId);

        // Assert
        assertNotNull(response);
        verify(groupMemberRepository, times(1)).delete(memberToRemove);
    }

    @Test
    void removeMember_LastAdmin() {
        // Arrange
        when(groupRepository.findByIdAndIsActiveTrue(1L)).thenReturn(Optional.of(testGroup));
        when(groupMemberRepository.findByGroupIdAndUserId(1L, testUserId))
                .thenReturn(Optional.of(testMember));
        when(groupMemberRepository.countAdminsByGroupId(1L)).thenReturn(1);

        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            groupService.removeMember(1L, testUserId, testUserId);
        });
    }

    @Test
    void updateMemberRole_Success() {
        // Arrange
        GroupMember memberToUpdate = GroupMember.builder()
                .id(2L)
                .userId(testUserId2)
                .role(GroupMember.MemberRole.MEMBER)
                .group(testGroup)
                .build();

        when(groupRepository.findByIdAndIsActiveTrue(1L)).thenReturn(Optional.of(testGroup));
        when(groupMemberRepository.findByGroupIdAndUserId(1L, testUserId))
                .thenReturn(Optional.of(testMember));
        when(groupMemberRepository.findByGroupIdAndUserId(1L, testUserId2))
                .thenReturn(Optional.of(memberToUpdate));
        when(groupMemberRepository.save(any(GroupMember.class))).thenReturn(memberToUpdate);
        when(groupRepository.save(any(Group.class))).thenReturn(testGroup);

        // Act
        GroupResponse response = groupService.updateMemberRole(1L, testUserId2, "ADMIN", testUserId);

        // Assert
        assertNotNull(response);
        verify(groupMemberRepository, times(1)).save(any(GroupMember.class));
    }

    @Test
    void updateMemberRole_InvalidRole() {
        // Arrange
        when(groupRepository.findByIdAndIsActiveTrue(1L)).thenReturn(Optional.of(testGroup));
        when(groupMemberRepository.findByGroupIdAndUserId(1L, testUserId))
                .thenReturn(Optional.of(testMember));
        when(groupMemberRepository.findByGroupIdAndUserId(1L, testUserId2))
                .thenReturn(Optional.of(testMember));

        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            groupService.updateMemberRole(1L, testUserId2, "INVALID_ROLE", testUserId);
        });
    }
}
