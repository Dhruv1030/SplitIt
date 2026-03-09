package com.splitwise.expense.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExpenseEventProducer {

    private static final String TOPIC = "expense-events";
    private final KafkaTemplate<String, ExpenseEvent> kafkaTemplate;

    @Async
    public void publishExpenseEvent(ExpenseEvent event) {
        try {
            kafkaTemplate.send(TOPIC, String.valueOf(event.getExpenseId()), event);
            log.info("Published {} event for expense: {}", event.getEventType(), event.getExpenseId());
        } catch (Exception e) {
            log.warn("Failed to publish expense event (non-blocking): {}", e.getMessage());
        }
    }
}
