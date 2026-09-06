package com.splitledger.expense_service.kafka;

import com.splitledger.expense_service.event.ExpenseCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExpenseEventPublisher {

    private static final String TOPIC = "expense.created";
    private final KafkaTemplate<String, ExpenseCreatedEvent> kafkaTemplate;

    public void publishExpenseCreated(ExpenseCreatedEvent event) {
        kafkaTemplate.send(TOPIC, event.getGroupId().toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish expense.created event: {}",
                                ex.getMessage());
                    } else {
                        log.info("Published expense.created event for expenseId: {}",
                                event.getExpenseId());
                    }
                });
    }
}
