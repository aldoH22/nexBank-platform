package com.nexbank.loan.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.nexbank.loan.enums.PaymentStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "loan_payments", indexes = {
    @Index(name = "idx_payment_loan_id", columnList = "loan_application_id"),
    @Index(name = "idx_payment_due_date", columnList = "dueDate")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "payment_seq")
    @SequenceGenerator(name = "payment_seq", sequenceName = "loan_payment_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_application_id", nullable = false)
    private LoanApplication loanApplication;

    // Número de cuota (1, 2, 3...)
    @Column(nullable = false)
    private Integer installmentNumber;

    // Fecha de vencimiento de esta cuota
    @Column(nullable = false)
    private LocalDate dueDate;

    // Monto total de la cuota
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    // Parte de capital en esta cuota
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal principalAmount;

    // Parte de interés en esta cuota
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal interestAmount;

    // Saldo restante después de esta cuota
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal remainingBalance;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column
    private LocalDateTime paidAt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
