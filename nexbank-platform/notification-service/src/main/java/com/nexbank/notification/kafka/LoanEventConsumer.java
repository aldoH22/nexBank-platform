package com.nexbank.notification.kafka;

import com.nexbank.common.event.LoanEvent;
import com.nexbank.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class LoanEventConsumer {

    private final NotificationService notificationService;

    @KafkaListener(
        topics = "${kafka.topics.loan-events}",
        groupId = "${spring.kafka.consumer.group-id}",
        containerFactory = "loanKafkaListenerContainerFactory"
    )
    public void consume(LoanEvent event) {
        log.info("Evento de préstamo recibido: loanId: {} status: {}",
                event.getLoanId(), event.getStatus());
        try {
            notificationService.processLoanEvent(event);
        } catch (Exception e) {
            log.error("Error procesando evento de préstamo: loanId: {} - {}",
                    event.getLoanId(), e.getMessage());
        }
    }
}
