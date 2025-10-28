package com.splitwise.group.repository;

import com.splitwise.group.model.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {

    Optional<Group> findByIdAndIsActiveTrue(Long id);

    List<Group> findByCreatedByAndIsActiveTrue(String createdBy);

    @Query("SELECT DISTINCT g FROM Group g JOIN g.members m WHERE m.userId = :userId AND g.isActive = true")
    List<Group> findGroupsByUserId(@Param("userId") String userId);

    @Query("SELECT g FROM Group g WHERE g.createdBy = :userId AND g.isActive = true")
    List<Group> findGroupsCreatedByUser(@Param("userId") String userId);

    @Query("SELECT COUNT(m) FROM GroupMember m WHERE m.group.id = :groupId")
    int countMembersByGroupId(@Param("groupId") Long groupId);

    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END FROM GroupMember m " +
            "WHERE m.group.id = :groupId AND m.userId = :userId")
    boolean isUserMemberOfGroup(@Param("groupId") Long groupId, @Param("userId") String userId);

    @Query("SELECT m.role FROM GroupMember m WHERE m.group.id = :groupId AND m.userId = :userId")
    Optional<String> getUserRoleInGroup(@Param("groupId") Long groupId, @Param("userId") String userId);
}
