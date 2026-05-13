package com.nexbank.loan.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.nexbank.loan.enums.LoanStatus;
import com.nexbank.loan.enums.LoanType;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "loan_applications", indexes = {
    @Index(name = "idx_loan_user_id", columnList = "userId"),
    @Index(name = "idx_loan_status", columnList = "status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "loan_seq")
    @SequenceGenerator(name = "loan_seq", sequenceName = "loan_application_seq", allocationSize = 1)
    private Long id;

    // Usuario que solicitó el préstamo
    @Column(nullable = false)
    private Long userId;

    // Cuenta donde se depositará el préstamo si es aprobado
    @Column(nullable = false)
    private Long disbursementAccountId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LoanType loanType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private LoanStatus status = LoanStatus.PENDING;

    // Monto solicitado
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal requestedAmount;

    // Tasa de interés anual aprobada (ej: 0.12 = 12%)
    @Column(precision = 5, scale = 4)
    private BigDecimal interestRate;

    // Plazo en meses
    @Column(nullable = false)
    private Integer termMonths;

    // Cuota mensual calculada
    @Column(precision = 15, scale = 2)
    private BigDecimal monthlyPayment;

    // Monto total a pagar (capital + intereses)
    @Column(precision = 15, scale = 2)
    private BigDecimal totalAmount;

    // Propósito declarado del préstamo
    @Column(length = 500)
    private String purpose;

    // Razón de rechazo si aplica
    @Column(length = 500)
    private String rejectionReason;

    // Score de crédito calculado
    @Column
    private Integer creditScore;

    // Ingresos mensuales declarados por el usuario
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal monthlyIncome;

    // Deudas mensuales declaradas
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal monthlyDebts;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column
    private LocalDateTime approvedAt;

    @Column
    private LocalDateTime rejectedAt;

    // Relación con pagos (se generan al aprobar)
    @OneToMany(mappedBy = "loanApplication", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<LoanPayment> payments = new ArrayList<>();
}
