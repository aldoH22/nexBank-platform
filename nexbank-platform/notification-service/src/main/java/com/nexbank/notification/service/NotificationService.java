package com.nexbank.notification.service;

import com.nexbank.common.event.LoanEvent;
import com.nexbank.common.event.TransactionEvent;
import com.nexbank.notification.document.NotificationLog;
import com.nexbank.notification.repository.NotificationLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationLogRepository repository;

    // ─────────────────────────────────────────────
    // PROCESAR EVENTO DE TRANSACCIÓN
    // ─────────────────────────────────────────────

    public void processTransactionEvent(TransactionEvent event) {
        log.info("Procesando evento de transacción: {} status: {}",
                event.getTransactionId(), event.getStatus());

        String type    = resolveTransactionType(event);
        String title   = resolveTransactionTitle(event);
        String message = resolveTransactionMessage(event);

        NotificationLog notification = NotificationLog.builder()
                .type(type)
                .userId(event.getInitiatedBy())
                .title(title)
                .message(message)
                .referenceId(event.getTransactionId())
                .referenceType("TRANSACTION")
                .amount(event.getAmount())
                .channel("EMAIL")
                .status("SENT")
                .createdAt(LocalDateTime.now())
                .build();

        repository.save(notification);

        // Simulación de envío — en producción aquí iría el cliente de email/SMS
        log.info("📧 [EMAIL] Para userId: {} | {} | {}", event.getInitiatedBy(), title, message);
    }

    // ─────────────────────────────────────────────
    // PROCESAR EVENTO DE PRÉSTAMO
    // ─────────────────────────────────────────────

    public void processLoanEvent(LoanEvent event) {
        log.info("Procesando evento de préstamo: {} status: {}",
                event.getLoanId(), event.getStatus());

        String type    = "LOAN_" + event.getStatus();
        String title   = resolveLoanTitle(event);
        String message = resolveLoanMessage(event);

        NotificationLog notification = NotificationLog.builder()
                .type(type)
                .userId(event.getUserId())
                .title(title)
                .message(message)
                .referenceId(event.getLoanId().toString())
                .referenceType("LOAN")
                .amount(event.getRequestedAmount())
                .channel("EMAIL")
                .status("SENT")
                .createdAt(LocalDateTime.now())
                .build();

        repository.save(notification);

        log.info("📧 [EMAIL] Para userId: {} | {} | {}", event.getUserId(), title, message);
    }

    // ─────────────────────────────────────────────
    // CONSULTAS
    // ─────────────────────────────────────────────

    public List<NotificationLog> getByUserId(Long userId) {
        return repository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<NotificationLog> getByReference(String referenceId, String referenceType) {
        return repository.findByReferenceIdAndReferenceType(referenceId, referenceType);
    }

    // ─────────────────────────────────────────────
    // HELPERS — mensajes
    // ─────────────────────────────────────────────

    private String resolveTransactionType(TransactionEvent event) {
        return switch (event.getType()) {
            case "TRANSFER"   -> "TRANSACTION_COMPLETED";
            case "DEPOSIT"    -> "DEPOSIT_COMPLETED";
            case "WITHDRAWAL" -> "WITHDRAWAL_COMPLETED";
            default           -> "TRANSACTION_COMPLETED";
        };
    }

    private String resolveTransactionTitle(TransactionEvent event) {
        return switch (event.getType()) {
            case "TRANSFER"   -> "Transferencia realizada";
            case "DEPOSIT"    -> "Depósito recibido";
            case "WITHDRAWAL" -> "Retiro procesado";
            default           -> "Transacción procesada";
        };
    }

    private String resolveTransactionMessage(TransactionEvent event) {
        return switch (event.getType()) {
            case "TRANSFER" -> String.format(
                "Se transfirieron $%.2f %s de la cuenta %d a la cuenta %d.",
                event.getAmount(), event.getCurrency(),
                event.getSourceAccountId(), event.getTargetAccountId());
            case "DEPOSIT" -> String.format(
                "Se depositaron $%.2f %s en la cuenta %d.",
                event.getAmount(), event.getCurrency(), event.getTargetAccountId());
            case "WITHDRAWAL" -> String.format(
                "Se retiraron $%.2f %s de la cuenta %d.",
                event.getAmount(), event.getCurrency(), event.getSourceAccountId());
            default -> String.format("Transacción por $%.2f %s procesada.",
                event.getAmount(), event.getCurrency());
        };
    }

    private String resolveLoanTitle(LoanEvent event) {
        return switch (event.getStatus()) {
            case "APPROVED"     -> "¡Préstamo aprobado!";
            case "REJECTED"     -> "Préstamo no aprobado";
            case "ACTIVE"       -> "Préstamo desembolsado";
            case "UNDER_REVIEW" -> "Préstamo en revisión";
            case "PAID"         -> "¡Préstamo pagado completamente!";
            default             -> "Actualización de préstamo";
        };
    }

    private String resolveLoanMessage(LoanEvent event) {
        return switch (event.getStatus()) {
            case "APPROVED" -> String.format(
                "Tu préstamo de $%.2f fue aprobado. Cuota mensual: $%.2f. Score crediticio: %d.",
                event.getRequestedAmount(), event.getMonthlyPayment(), event.getCreditScore());
            case "REJECTED" -> String.format(
                "Tu solicitud de $%.2f no fue aprobada. Motivo: %s",
                event.getRequestedAmount(), event.getRejectionReason());
            case "ACTIVE" -> String.format(
                "Tu préstamo #%d ha sido desembolsado. ¡Ya puedes disponer de los fondos!",
                event.getLoanId());
            case "UNDER_REVIEW" -> String.format(
                "Tu solicitud de $%.2f está en revisión manual. Te notificaremos pronto.",
                event.getRequestedAmount());
            case "PAID" -> String.format(
                "¡Felicidades! Has pagado completamente tu préstamo #%d.",
                event.getLoanId());
            default -> "Tu préstamo ha sido actualizado.";
        };
    }
}
