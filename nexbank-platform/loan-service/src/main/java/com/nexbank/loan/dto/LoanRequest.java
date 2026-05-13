package com.nexbank.loan.dto;

import java.math.BigDecimal;

import com.nexbank.loan.enums.LoanType;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoanRequest {

    @NotNull(message = "El userId es requerido")
    private Long userId;

    @NotNull(message = "La cuenta de desembolso es requerida")
    private Long disbursementAccountId;

    @NotNull(message = "El tipo de préstamo es requerido")
    private LoanType loanType;

    @NotNull(message = "El monto es requerido")
    @DecimalMin(value = "1000.00", message = "El monto mínimo es $1,000")
    @DecimalMax(value = "5000000.00", message = "El monto máximo es $5,000,000")
    private BigDecimal requestedAmount;

    @NotNull(message = "El plazo es requerido")
    @Min(value = 6, message = "El plazo mínimo es 6 meses")
    @Max(value = 360, message = "El plazo máximo es 360 meses")
    private Integer termMonths;

    @NotNull(message = "Los ingresos mensuales son requeridos")
    @DecimalMin(value = "0.01", message = "Los ingresos deben ser mayores a 0")
    private BigDecimal monthlyIncome;

    @NotNull(message = "Las deudas mensuales son requeridas")
    @DecimalMin(value = "0.00", message = "Las deudas no pueden ser negativas")
    private BigDecimal monthlyDebts;

    @Size(max = 500, message = "El propósito no puede exceder 500 caracteres")
    private String purpose;
}
