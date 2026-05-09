package com.nexbank.transaction.client;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import com.nexbank.transaction.dto.AccountResponse;
import com.nexbank.transaction.exception.TransactionException;

@Component
public class AccountServiceClientFallback implements AccountServiceClient {

    @Override
    public AccountResponse getAccountById(Long accountId) {
        throw new TransactionException(
            "account-service no disponible al consultar cuenta: " + accountId);
    }

    @Override
    public AccountResponse deposit(Long accountId, BigDecimal amount) {
        throw new TransactionException(
            "account-service no disponible al depositar en cuenta: " + accountId);
    }

    @Override
    public AccountResponse withdraw(Long accountId, BigDecimal amount) {
        throw new TransactionException(
            "account-service no disponible al retirar de cuenta: " + accountId);
    }
}