package com.nexbank.transaction.exception;

public class InsufficientFundsException extends RuntimeException {
    public InsufficientFundsException(Long accountId) {
        super("Saldo insuficiente en la cuenta: " + accountId);
    }
}
