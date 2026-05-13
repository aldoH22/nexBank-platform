package com.nexbank.loan.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PayLoanInstallmentRequest {

    @NotNull(message = "El paymentId es requerido")
    private Long paymentId;

    // Cuenta desde la que se debita el pago
    @NotNull(message = "La cuenta de origen es requerida")
    private Long sourceAccountId;
}
