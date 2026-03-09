package com.splitwise.group.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class GroupEventProducer {

    private static final String TOPIC = "group-events";
    private final KafkaTemplate<String, GroupEvent> kafkaTemplate;

    @Async
    public void publishGroupEvent(GroupEvent event) {
        try {
            kafkaTemplate.send(TOPIC, String.valueOf(event.getGroupId()), event);
            log.info("Published {} event for group: {}", event.getEventType(), event.getGroupId());
        } catch (Exception e) {
            log.warn("Failed to publish group event (non-blocking): {}", e.getMessage());
        }
    }
}
