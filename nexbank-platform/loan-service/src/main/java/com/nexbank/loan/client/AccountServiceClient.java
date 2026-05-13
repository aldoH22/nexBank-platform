package com.nexbank.loan.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.nexbank.loan.dto.AccountResponse;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

@FeignClient(name = "ACCOUNT-SERVICE", path = "/api/v1/accounts")
public interface AccountServiceClient {

    @GetMapping("/{accountId}")
    @CircuitBreaker(name = "account-service", fallbackMethod = "getAccountFallback")
    AccountResponse getAccountById(@PathVariable Long accountId);

    default AccountResponse getAccountFallback(Long accountId, Exception ex) {
        throw new RuntimeException("account-service no disponible. Intenta más tarde.");
    }
}
