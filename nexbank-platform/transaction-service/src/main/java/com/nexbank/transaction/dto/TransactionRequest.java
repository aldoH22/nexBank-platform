package com.nexbank.transaction.dto;

import java.math.BigDecimal;

import com.nexbank.transaction.enums.TransactionType;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionRequest {

    /**
     * ID único generado por el cliente (UUID)
     * Sirve para idempotencia — si se reenvía el mismo request, no se duplica
     */
    @NotBlank(message = "El transactionId es obligatorio")
    private String transactionId;

    @NotNull(message = "El tipo de transacción es obligatorio")
    private TransactionType type;

    /**
     * Cuenta origen (requerida en TRANSFER y WITHDRAWAL)
     */
    private Long sourceAccountId;

    /**
     * Cuenta destino (requerida en TRANSFER y DEPOSIT)
     */
    private Long targetAccountId;

    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a cero")
    private BigDecimal amount;

    @NotBlank(message = "La moneda es obligatoria")
    @Size(min = 3, max = 3, message = "La moneda debe tener 3 caracteres (ej: MXN)")
    private String currency;

    @Size(max = 255)
    private String description;
}
