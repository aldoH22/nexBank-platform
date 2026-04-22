package com.nexbank.account.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad Account - Representa una cuenta bancaria.
 * 
 * Características:
 * - Cada cuenta pertenece a un usuario (relación con auth-service)
 * - Diferentes tipos: Ahorro, Corriente, Inversión
 * - Control de saldo y límites de transacción
 * - Estados: Activa, Inactiva, Bloqueada, Cerrada
 * - Auditoría con createdAt y updatedAt
 */
@Entity
@Table(name = "accounts", indexes = {
    @Index(name = "idx_account_number", columnList = "accountNumber"),
    @Index(name = "idx_user_id", columnList = "userId")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Account {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Número de cuenta único (ej: 1234567890)
     * Se genera automáticamente al crear la cuenta
     */
    @Column(nullable = false, unique = true, length = 20)
    private String accountNumber;
    
    /**
     * ID del usuario dueño de la cuenta
     * Este campo se valida contra auth-service
     */
    @Column(nullable = false)
    private Long userId;
    
    /**
     * Tipo de cuenta
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccountType accountType;
    
    /**
     * Saldo actual de la cuenta
     * Debe ser >= 0 para cuentas de ahorro
     * Puede ser negativo para cuentas corrientes (sobregiro)
     */
    @Column(nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;
    
    /**
     * Moneda de la cuenta (USD, MXN, EUR, etc.)
     */
    @Column(nullable = false, length = 3)
    @Builder.Default
    private String currency = "MXN";
    
    /**
     * Estado de la cuenta
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private AccountStatus status = AccountStatus.ACTIVE;
    
    /**
     * Límite diario de transferencias
     * null = sin límite
     */
    @Column(precision = 15, scale = 2)
    private BigDecimal dailyTransactionLimit;
    
    /**
     * Alias de la cuenta (ej: "Mi cuenta de ahorro")
     */
    @Column(length = 50)
    private String alias;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    /**
     * Fecha de cierre (si la cuenta fue cerrada)
     */
    private LocalDateTime closedAt;
    
    /**
     * Verifica si la cuenta está activa
     */
    public boolean isActive() {
        return this.status == AccountStatus.ACTIVE;
    }
    
    /**
     * Verifica si se puede realizar una transacción
     */
    public boolean canTransact() {
        return this.status == AccountStatus.ACTIVE;
    }
    
    /**
     * Verifica si el saldo es suficiente
     */
    public boolean hasSufficientBalance(BigDecimal amount) {
        return this.balance.compareTo(amount) >= 0;
    }
    
    /**
     * Deposita dinero en la cuenta
     */
    public void deposit(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor a cero");
        }
        this.balance = this.balance.add(amount);
    }
    
    /**
     * Retira dinero de la cuenta
     */
    public void withdraw(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor a cero");
        }
        if (!hasSufficientBalance(amount)) {
            throw new IllegalArgumentException("Saldo insuficiente");
        }
        this.balance = this.balance.subtract(amount);
    }
}
