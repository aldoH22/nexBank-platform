package com.nexbank.loan.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.nexbank.loan.enums.PaymentStatus;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoanPaymentResponse {
    private Long id;
    private Long loanApplicationId;
    private Integer installmentNumber;
    private LocalDate dueDate;
    private BigDecimal amount;
    private BigDecimal principalAmount;
    private BigDecimal interestAmount;
    private BigDecimal remainingBalance;
    private PaymentStatus status;
    private LocalDateTime paidAt;
}
