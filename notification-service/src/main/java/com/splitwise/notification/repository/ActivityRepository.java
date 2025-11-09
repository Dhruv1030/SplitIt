package com.splitwise.notification.repository;

import com.splitwise.notification.model.Activity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, Long> {

    // Get activities for a group (paginated)
    Page<Activity> findByGroupIdOrderByTimestampDesc(Long groupId, Pageable pageable);

    // Get activities for a user across all groups
    @Query("SELECT a FROM Activity a WHERE a.userId = :userId OR a.targetUserId = :userId ORDER BY a.timestamp DESC")
    Page<Activity> findByUserIdOrderByTimestampDesc(@Param("userId") String userId, Pageable pageable);

    // Get recent activities for a group (last N)
    List<Activity> findTop10ByGroupIdOrderByTimestampDesc(Long groupId);

    // Get activities by date range
    List<Activity> findByGroupIdAndTimestampBetweenOrderByTimestampDesc(
            Long groupId,
            LocalDateTime startDate,
            LocalDateTime endDate);

    // Count activities for a group
    long countByGroupId(Long groupId);
}
