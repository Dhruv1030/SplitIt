package com.splitwise.analytics.repository;

import com.splitwise.analytics.model.AnalyticsEvent;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AnalyticsEventRepository extends MongoRepository<AnalyticsEvent, String> {
    List<AnalyticsEvent> findByUserIdOrderByTimestampDesc(String userId);
    List<AnalyticsEvent> findByGroupIdOrderByTimestampDesc(Long groupId);
    List<AnalyticsEvent> findByEventTypeAndTimestampBetween(String eventType, LocalDateTime start, LocalDateTime end);
    long countByEventType(String eventType);
}
