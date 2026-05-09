package com.nexbank.transaction.exception;

public class AccountNotFoundException extends RuntimeException {
    public AccountNotFoundException(Long accountId) {
        super("Cuenta no encontrada con ID: " + accountId);
    }
}
