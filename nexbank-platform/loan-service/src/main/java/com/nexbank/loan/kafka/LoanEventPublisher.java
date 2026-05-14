package com.nexbank.loan.kafka;

import com.nexbank.common.event.LoanEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class LoanEventPublisher {

    private final KafkaTemplate<String, LoanEvent> kafkaTemplate;

    @Value("${kafka.topics.loan-events}")
    private String topic;

    public void publish(LoanEvent event) {
        kafkaTemplate.send(topic, event.getLoanId().toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Error publicando evento de préstamo: {} - {}",
                                event.getLoanId(), ex.getMessage());
                    } else {
                        log.info("Evento publicado en Kafka - topic: {} loanId: {}",
                                topic, event.getLoanId());
                    }
                });
    }
}