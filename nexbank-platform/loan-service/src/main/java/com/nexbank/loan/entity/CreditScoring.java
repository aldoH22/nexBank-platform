package com.nexbank.loan.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "credit_scoring", indexes = {
    @Index(name = "idx_scoring_user_id", columnList = "userId"),
    @Index(name = "idx_scoring_loan_id", columnList = "loan_application_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditScoring {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "scoring_seq")
    @SequenceGenerator(name = "scoring_seq", sequenceName = "credit_scoring_seq", allocationSize = 1)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_application_id", nullable = false)
    private LoanApplication loanApplication;

    // Score final (300 - 850)
    @Column(nullable = false)
    private Integer finalScore;

    // Puntos por cada criterio evaluado
    @Column(nullable = false)
    private Integer incomeScore;       // Puntaje por ingresos

    @Column(nullable = false)
    private Integer debtRatioScore;    // Puntaje por ratio deuda/ingreso

    @Column(nullable = false)
    private Integer loanAmountScore;   // Puntaje por monto solicitado vs ingresos

    @Column(nullable = false)
    private Integer loanTypeScore;     // Puntaje según tipo de préstamo

    // Ratio deuda/ingreso calculado
    @Column(nullable = false, precision = 5, scale = 4)
    private BigDecimal debtToIncomeRatio;

    // Decisión automática
    @Column(nullable = false, length = 20)
    private String decision;  // APPROVED, REJECTED, MANUAL_REVIEW

    // Justificación de la decisión
    @Column(length = 1000)
    private String decisionReason;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
