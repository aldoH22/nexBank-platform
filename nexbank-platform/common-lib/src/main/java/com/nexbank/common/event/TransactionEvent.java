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
public class TransactionEvent {

    private String transactionId;
    private String type;          // TRANSFER, DEPOSIT, WITHDRAWAL
    private String status;        // COMPLETED, FAILED
    private Long sourceAccountId;
    private Long targetAccountId;
    private BigDecimal amount;
    private String currency;
    private String description;
    private Long initiatedBy;     // userId
    private LocalDateTime processedAt;
}