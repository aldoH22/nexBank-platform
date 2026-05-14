package com.nexbank.notification.kafka;

import com.nexbank.common.event.TransactionEvent;
import com.nexbank.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionEventConsumer {

    private final NotificationService notificationService;

    @KafkaListener(
        topics = "${kafka.topics.transaction-events}",
        groupId = "${spring.kafka.consumer.group-id}",
        containerFactory = "transactionKafkaListenerContainerFactory"
    )
    public void consume(TransactionEvent event) {
        log.info("Evento de transacción recibido: {} tipo: {}",
                event.getTransactionId(), event.getType());
        try {
            notificationService.processTransactionEvent(event);
        } catch (Exception e) {
            log.error("Error procesando evento de transacción: {} - {}",
                    event.getTransactionId(), e.getMessage());
        }
    }
}
