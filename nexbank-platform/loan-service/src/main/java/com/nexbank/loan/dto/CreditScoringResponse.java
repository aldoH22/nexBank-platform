package com.nexbank.loan.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreditScoringResponse {
    private Long id;
    private Long userId;
    private Long loanApplicationId;
    private Integer finalScore;
    private Integer incomeScore;
    private Integer debtRatioScore;
    private Integer loanAmountScore;
    private Integer loanTypeScore;
    private BigDecimal debtToIncomeRatio;
    private String decision;
    private String decisionReason;
    private LocalDateTime createdAt;
}
