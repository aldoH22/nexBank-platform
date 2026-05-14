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
public class LoanEvent {

    private Long loanId;
    private Long userId;
    private String loanType;      // PERSONAL, MORTGAGE, AUTOMOTIVE
    private String status;        // APPROVED, REJECTED, ACTIVE, PAID
    private BigDecimal requestedAmount;
    private BigDecimal monthlyPayment;
    private Integer creditScore;
    private String rejectionReason;
    private LocalDateTime occurredAt;
}