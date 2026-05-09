package com.nexbank.transaction.client;

import java.math.BigDecimal;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.nexbank.transaction.dto.AccountResponse;

@FeignClient(
    name = "ACCOUNT-SERVICE",
    fallback = AccountServiceClientFallback.class
)
public interface AccountServiceClient {

    @GetMapping("/api/v1/accounts/{accountId}")
    AccountResponse getAccountById(
        @PathVariable("accountId") Long accountId
    );

    @PutMapping("/api/v1/accounts/{accountId}/deposit")
    AccountResponse deposit(
        @PathVariable("accountId") Long accountId,
        @RequestParam("amount") BigDecimal amount
    );

    @PutMapping("/api/v1/accounts/{accountId}/withdraw")
    AccountResponse withdraw(
        @PathVariable("accountId") Long accountId,
        @RequestParam("amount") BigDecimal amount
    );
}
