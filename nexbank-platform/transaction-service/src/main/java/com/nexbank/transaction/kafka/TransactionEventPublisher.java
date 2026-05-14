package com.nexbank.transaction.kafka;

import com.nexbank.common.event.TransactionEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionEventPublisher {

    private final KafkaTemplate<String, TransactionEvent> kafkaTemplate;

    @Value("${kafka.topics.transaction-events}")
    private String topic;

    public void publish(TransactionEvent event) {
        kafkaTemplate.send(topic, event.getTransactionId(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Error publicando evento de transacción: {} - {}",
                                event.getTransactionId(), ex.getMessage());
                    } else {
                        log.info("Evento publicado en Kafka - topic: {} transactionId: {}",
                                topic, event.getTransactionId());
                    }
                });
    }
}