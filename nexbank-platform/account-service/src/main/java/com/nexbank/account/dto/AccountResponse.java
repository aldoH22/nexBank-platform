package com.nexbank.account.dto;

import com.nexbank.account.entity.AccountStatus;
import com.nexbank.account.entity.AccountType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO de respuesta con información de una cuenta.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountResponse {
    
    private Long id;
    private String accountNumber;
    private Long userId;
    private String username;
    private AccountType accountType;
    private BigDecimal balance;
    private String currency;
    private AccountStatus status;
    private BigDecimal dailyTransactionLimit;
    private String alias;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
