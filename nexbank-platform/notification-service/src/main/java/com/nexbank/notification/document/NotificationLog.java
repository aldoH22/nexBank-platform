package com.nexbank.notification.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Document(collection = "notification_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationLog {

    @Id
    private String id;

    // Tipo de notificación
    @Indexed
    private String type;  // TRANSACTION_COMPLETED, TRANSACTION_FAILED,
                          // LOAN_APPROVED, LOAN_REJECTED, LOAN_ACTIVE, LOAN_PAID

    // Usuario al que va dirigida
    @Indexed
    private Long userId;

    // Título y mensaje de la notificación
    private String title;
    private String message;

    // Datos del evento original
    private String referenceId;     // transactionId o loanId
    private String referenceType;   // TRANSACTION o LOAN
    private BigDecimal amount;

    // Canal por el que se "enviaría" (simulado con logs)
    private String channel;  // EMAIL, SMS, PUSH

    // Estado del envío
    private String status;   // SENT, FAILED

    @Indexed(expireAfterSeconds = 2592000) // TTL 30 días
    private LocalDateTime createdAt;
}
