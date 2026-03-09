package com.splitwise.analytics.consumer;

import com.splitwise.analytics.event.ExpenseEvent;
import com.splitwise.analytics.event.GroupEvent;
import com.splitwise.analytics.event.SettlementEvent;
import com.splitwise.analytics.model.AnalyticsEvent;
import com.splitwise.analytics.repository.AnalyticsEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class AnalyticsEventConsumer {

    private final AnalyticsEventRepository repository;

    @KafkaListener(topics = "expense-events", groupId = "analytics-service")
    public void consumeExpenseEvent(ExpenseEvent event) {
        log.info("Received expense event: {} for expense: {}", event.getEventType(), event.getExpenseId());
        AnalyticsEvent analyticsEvent = AnalyticsEvent.builder()
                .eventType(event.getEventType())
                .source("expense-service")
                .userId(event.getPaidByUserId())
                .groupId(event.getGroupId())
                .data(Map.of(
                        "expenseId", event.getExpenseId(),
                        "amount", event.getAmount(),
                        "description", event.getDescription(),
                        "category", event.getCategory() != null ? event.getCategory() : "uncategorized"
                ))
                .timestamp(event.getTimestamp())
                .createdAt(LocalDateTime.now())
                .build();
        repository.save(analyticsEvent);
    }

    @KafkaListener(topics = "group-events", groupId = "analytics-service")
    public void consumeGroupEvent(GroupEvent event) {
        log.info("Received group event: {} for group: {}", event.getEventType(), event.getGroupId());
        AnalyticsEvent analyticsEvent = AnalyticsEvent.builder()
                .eventType(event.getEventType())
                .source("group-service")
                .userId(event.getUserId())
                .groupId(event.getGroupId())
                .data(Map.of(
                        "groupName", event.getGroupName() != null ? event.getGroupName() : "",
                        "targetUserId", event.getTargetUserId() != null ? event.getTargetUserId() : ""
                ))
                .timestamp(event.getTimestamp())
                .createdAt(LocalDateTime.now())
                .build();
        repository.save(analyticsEvent);
    }

    @KafkaListener(topics = "settlement-events", groupId = "analytics-service")
    public void consumeSettlementEvent(SettlementEvent event) {
        log.info("Received settlement event: {} for settlement: {}", event.getEventType(), event.getSettlementId());
        AnalyticsEvent analyticsEvent = AnalyticsEvent.builder()
                .eventType(event.getEventType())
                .source("settlement-service")
                .userId(event.getPayerUserId())
                .groupId(event.getGroupId())
                .data(Map.of(
                        "settlementId", event.getSettlementId(),
                        "amount", event.getAmount(),
                        "payeeUserId", event.getPayeeUserId()
                ))
                .timestamp(event.getTimestamp())
                .createdAt(LocalDateTime.now())
                .build();
        repository.save(analyticsEvent);
    }
}
