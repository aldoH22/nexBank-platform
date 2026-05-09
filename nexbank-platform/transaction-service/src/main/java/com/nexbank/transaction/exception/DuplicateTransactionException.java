package com.nexbank.transaction.exception;

public class DuplicateTransactionException extends RuntimeException {
    public DuplicateTransactionException(String transactionId) {
        super("Ya existe una transacción con el ID: " + transactionId);
    }
}
