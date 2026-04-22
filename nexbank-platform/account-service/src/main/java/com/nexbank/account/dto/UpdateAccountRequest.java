package com.nexbank.account.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO para actualizar información de una cuenta.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAccountRequest {
    
    @Size(max = 50, message = "Alias debe tener máximo 50 caracteres")
    private String alias;
    
    @DecimalMin(value = "0.0", message = "El límite debe ser mayor o igual a 0")
    private BigDecimal dailyTransactionLimit;
}
