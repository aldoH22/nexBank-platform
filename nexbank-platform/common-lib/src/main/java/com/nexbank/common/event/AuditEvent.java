package com.nexbank.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditEvent {

    private String eventId;          // UUID único del evento
    private String eventType;        // TRANSACTION, LOAN
    private String action;           // TRANSFER, DEPOSIT, WITHDRAWAL, LOAN_APPROVED, etc.
    private String status;           // COMPLETED, FAILED, APPROVED, REJECTED, etc.
    private Long userId;
    private String referenceId;      // transactionId o loanId
    private BigDecimal amount;
    private String currency;
    private String description;
    private String sourceService;    // transaction-service, loan-service
    private LocalDateTime occurredAt;
}