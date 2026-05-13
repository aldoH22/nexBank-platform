package com.nexbank.loan.enums;

public enum LoanStatus {
    PENDING,        // Recién creada, esperando scoring
    UNDER_REVIEW,   // En revisión manual (montos altos)
    APPROVED,       // Aprobada automáticamente
    REJECTED,       // Rechazada por scoring
    ACTIVE,         // Desembolsada, con pagos en curso
    PAID,           // Completamente pagada
    DEFAULTED       // En mora
}
