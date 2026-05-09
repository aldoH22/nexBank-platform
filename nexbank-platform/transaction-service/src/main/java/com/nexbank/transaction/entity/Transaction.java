package com.nexbank.transaction.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.nexbank.transaction.enums.TransactionStatus;
import com.nexbank.transaction.enums.TransactionType;

import jakarta.persistence.Table;
import jakarta.persistence.Index;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "TRANSACTIONS", indexes = {
    @Index(name = "idx_transaction_id", columnList = "transactionId"),
    @Index(name = "idx_source_account", columnList = "sourceAccountId"),
    @Index(name = "idx_target_account", columnList = "targetAccountId")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "transaction_seq")
    @SequenceGenerator(name = "transaction_seq", sequenceName = "TRANSACTION_SEQ", allocationSize = 1)
    private Long id;

    /**
     * ID único de la transacción (para idempotencia)
     * El cliente lo genera y lo envía en el request
     */
    @Column(nullable = false, unique = true, length = 36)
    private String transactionId;

    /**
     * Tipo de transacción
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionType type;

    /**
     * Estado actual de la transacción
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private TransactionStatus status = TransactionStatus.PENDING;

    /**
     * ID de la cuenta origen (null en depósitos)
     */
    @Column
    private Long sourceAccountId;

    /**
     * ID de la cuenta destino (null en retiros)
     */
    @Column
    private Long targetAccountId;

    /**
     * Monto de la transacción
     */
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    /**
     * Moneda de la transacción
     */
    @Column(nullable = false, length = 3)
    @Builder.Default
    private String currency = "MXN";

    /**
     * Descripción o concepto de la transacción
     */
    @Column(length = 255)
    private String description;

    /**
     * Mensaje de error si la transacción falló
     */
    @Column(length = 500)
    private String errorMessage;

    /**
     * ID del usuario que inició la transacción
     */
    @Column(nullable = false)
    private Long initiatedBy;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Fecha en que se completó o falló
     */
    @Column
    private LocalDateTime processedAt;
}
