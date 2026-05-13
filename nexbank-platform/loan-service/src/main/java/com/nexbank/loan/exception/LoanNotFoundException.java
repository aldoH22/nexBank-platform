package com.nexbank.loan.exception;

public class LoanNotFoundException extends RuntimeException {
    public LoanNotFoundException(Long id) {
        super("Préstamo no encontrado con id: " + id);
    }
}
