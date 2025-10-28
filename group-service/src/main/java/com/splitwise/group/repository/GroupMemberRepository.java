package com.splitwise.group.repository;

import com.splitwise.group.model.GroupMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {

    List<GroupMember> findByGroupId(Long groupId);

    List<GroupMember> findByUserId(String userId);

    Optional<GroupMember> findByGroupIdAndUserId(Long groupId, String userId);

    boolean existsByGroupIdAndUserId(Long groupId, String userId);

    @Modifying
    @Query("DELETE FROM GroupMember m WHERE m.group.id = :groupId AND m.userId = :userId")
    void deleteByGroupIdAndUserId(@Param("groupId") Long groupId, @Param("userId") String userId);

    @Query("SELECT COUNT(m) FROM GroupMember m WHERE m.group.id = :groupId AND m.role = 'ADMIN'")
    int countAdminsByGroupId(@Param("groupId") Long groupId);
}
