package com.splitwise.group.repository;

import com.splitwise.group.model.Group;
import com.splitwise.group.model.GroupMember;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class GroupRepositoryIntegrationTest {

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private GroupMemberRepository groupMemberRepository;

    private Group savedGroup;
    private final String userId = "user-123";
    private final String userId2 = "user-456";

    @BeforeEach
    void setUp() {
        groupMemberRepository.deleteAll();
        groupRepository.deleteAll();

        Group group = Group.builder()
                .name("Test Group")
                .description("Integration test group")
                .category("TRIP")
                .createdBy(userId)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .isActive(true)
                .members(new ArrayList<>())
                .build();

        savedGroup = groupRepository.save(group);

        GroupMember member = GroupMember.builder()
                .userId(userId)
                .role(GroupMember.MemberRole.ADMIN)
                .joinedAt(LocalDateTime.now())
                .group(savedGroup)
                .build();
        groupMemberRepository.save(member);

        GroupMember member2 = GroupMember.builder()
                .userId(userId2)
                .role(GroupMember.MemberRole.MEMBER)
                .joinedAt(LocalDateTime.now())
                .group(savedGroup)
                .build();
        groupMemberRepository.save(member2);
    }

    @Test
    void findByIdAndIsActiveTrue_returnsGroup() {
        Optional<Group> found = groupRepository.findByIdAndIsActiveTrue(savedGroup.getId());
        assertTrue(found.isPresent());
        assertEquals("Test Group", found.get().getName());
    }

    @Test
    void findByIdAndIsActiveTrue_returnsEmptyForInactive() {
        savedGroup.setActive(false);
        groupRepository.save(savedGroup);

        Optional<Group> found = groupRepository.findByIdAndIsActiveTrue(savedGroup.getId());
        assertTrue(found.isEmpty());
    }

    @Test
    void findGroupsByUserId_returnsUserGroups() {
        List<Group> groups = groupRepository.findGroupsByUserId(userId);
        assertEquals(1, groups.size());
        assertEquals("Test Group", groups.get(0).getName());
    }

    @Test
    void findGroupsByUserId_returnsEmptyForNonMember() {
        List<Group> groups = groupRepository.findGroupsByUserId("nonexistent");
        assertTrue(groups.isEmpty());
    }

    @Test
    void isUserMemberOfGroup_returnsTrue() {
        assertTrue(groupRepository.isUserMemberOfGroup(savedGroup.getId(), userId));
    }

    @Test
    void isUserMemberOfGroup_returnsFalse() {
        assertFalse(groupRepository.isUserMemberOfGroup(savedGroup.getId(), "stranger"));
    }

    @Test
    void countMembersByGroupId_returnsCorrectCount() {
        int count = groupRepository.countMembersByGroupId(savedGroup.getId());
        assertEquals(2, count);
    }

    @Test
    void existsByGroupIdAndUserId_returnsTrue() {
        assertTrue(groupMemberRepository.existsByGroupIdAndUserId(savedGroup.getId(), userId));
    }

    @Test
    void countAdminsByGroupId_returnsCorrectCount() {
        int adminCount = groupMemberRepository.countAdminsByGroupId(savedGroup.getId());
        assertEquals(1, adminCount);
    }

    @Test
    void findByGroupIdAndUserId_returnsMember() {
        Optional<GroupMember> member = groupMemberRepository.findByGroupIdAndUserId(savedGroup.getId(), userId2);
        assertTrue(member.isPresent());
        assertEquals(GroupMember.MemberRole.MEMBER, member.get().getRole());
    }
}
