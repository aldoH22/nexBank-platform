package com.nexbank.loan.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.nexbank.loan.enums.LoanStatus;
import com.nexbank.loan.enums.LoanType;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoanResponse {
    private Long id;
    private Long userId;
    private Long disbursementAccountId;
    private LoanType loanType;
    private LoanStatus status;
    private BigDecimal requestedAmount;
    private BigDecimal interestRate;
    private Integer termMonths;
    private BigDecimal monthlyPayment;
    private BigDecimal totalAmount;
    private String purpose;
    private String rejectionReason;
    private Integer creditScore;
    private BigDecimal monthlyIncome;
    private BigDecimal monthlyDebts;
    private LocalDateTime createdAt;
    private LocalDateTime approvedAt;
    private LocalDateTime rejectedAt;
}
