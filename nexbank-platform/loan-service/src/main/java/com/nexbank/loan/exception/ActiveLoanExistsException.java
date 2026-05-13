package com.nexbank.loan.exception;

public class ActiveLoanExistsException extends RuntimeException {
    public ActiveLoanExistsException(Long userId) {
        super("El usuario " + userId + " ya tiene un préstamo activo o en revisión");
    }
}