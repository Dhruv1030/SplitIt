package com.splitwise.settlement.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SettlementEventProducer {

    private static final String TOPIC = "settlement-events";
    private final KafkaTemplate<String, SettlementEvent> kafkaTemplate;

    @Async
    public void publishSettlementEvent(SettlementEvent event) {
        try {
            kafkaTemplate.send(TOPIC, String.valueOf(event.getSettlementId()), event);
            log.info("Published {} event for settlement: {}", event.getEventType(), event.getSettlementId());
        } catch (Exception e) {
            log.warn("Failed to publish settlement event (non-blocking): {}", e.getMessage());
        }
    }
}
