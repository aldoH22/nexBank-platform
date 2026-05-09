package com.nexbank.transaction.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.nexbank.transaction.enums.TransactionStatus;
import com.nexbank.transaction.enums.TransactionType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {

    private Long id;
    private String transactionId;
    private TransactionType type;
    private TransactionStatus status;
    private Long sourceAccountId;
    private Long targetAccountId;
    private BigDecimal amount;
    private String currency;
    private String description;
    private String errorMessage;
    private Long initiatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;
}
