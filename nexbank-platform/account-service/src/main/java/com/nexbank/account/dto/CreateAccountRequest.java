package com.nexbank.account.dto;

import com.nexbank.account.entity.AccountType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO para crear una nueva cuenta bancaria.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateAccountRequest {
    
    @NotNull(message = "User ID es obligatorio")
    private Long userId;
    
    @NotNull(message = "Tipo de cuenta es obligatorio")
    private AccountType accountType;
    
    @DecimalMin(value = "0.0", message = "El saldo inicial debe ser mayor o igual a 0")
    private BigDecimal initialBalance;
    
    @Size(max = 3, message = "Moneda debe tener máximo 3 caracteres")
    private String currency;
    
    @Size(max = 50, message = "Alias debe tener máximo 50 caracteres")
    private String alias;
    
    private BigDecimal dailyTransactionLimit;
}