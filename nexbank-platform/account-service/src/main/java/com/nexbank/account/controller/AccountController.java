package com.nexbank.account.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nexbank.account.dto.AccountResponse;
import com.nexbank.account.dto.CreateAccountRequest;
import com.nexbank.account.dto.UpdateAccountRequest;
import com.nexbank.account.service.AccountService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
@Slf4j
public class AccountController {

    private final AccountService accountService;

    // ─────────────────────────────────────────────
    // POST /api/v1/accounts
    // Crear una nueva cuenta
    // ─────────────────────────────────────────────
    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(
            @Valid @RequestBody CreateAccountRequest request) {
        log.info("POST /api/v1/accounts - userId: {}", request.getUserId());
        AccountResponse response = accountService.createAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ─────────────────────────────────────────────
    // GET /api/v1/accounts/{accountId}
    // Obtener cuenta por ID
    // ─────────────────────────────────────────────
    @GetMapping("/{accountId}")
    public ResponseEntity<AccountResponse> getAccountById(
            @PathVariable Long accountId) {
        log.info("GET /api/v1/accounts/{}", accountId);
        return ResponseEntity.ok(accountService.getAccountById(accountId));
    }

    // ─────────────────────────────────────────────
    // GET /api/v1/accounts/number/{accountNumber}
    // Obtener cuenta por número de cuenta
    // ─────────────────────────────────────────────
    @GetMapping("/number/{accountNumber}")
    public ResponseEntity<AccountResponse> getAccountByNumber(
            @PathVariable String accountNumber) {
        log.info("GET /api/v1/accounts/number/{}", accountNumber);
        return ResponseEntity.ok(accountService.getAccountByNumber(accountNumber));
    }

    // ─────────────────────────────────────────────
    // GET /api/v1/accounts/user/{userId}
    // Obtener todas las cuentas de un usuario
    // ─────────────────────────────────────────────
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AccountResponse>> getAccountsByUserId(
            @PathVariable Long userId) {
        log.info("GET /api/v1/accounts/user/{}", userId);
        return ResponseEntity.ok(accountService.getAccountsByUserId(userId));
    }

    // ─────────────────────────────────────────────
    // PUT /api/v1/accounts/{accountId}
    // Actualizar alias y/o límite diario
    // ─────────────────────────────────────────────
    @PutMapping("/{accountId}")
    public ResponseEntity<AccountResponse> updateAccount(
            @PathVariable Long accountId,
            @Valid @RequestBody UpdateAccountRequest request) {
        log.info("PUT /api/v1/accounts/{}", accountId);
        return ResponseEntity.ok(accountService.updateAccount(accountId, request));
    }

    // ─────────────────────────────────────────────
    // PATCH /api/v1/accounts/{accountId}/block
    // Bloquear cuenta
    // ─────────────────────────────────────────────
    @PatchMapping("/{accountId}/block")
    public ResponseEntity<AccountResponse> blockAccount(
            @PathVariable Long accountId) {
        log.info("PATCH /api/v1/accounts/{}/block", accountId);
        return ResponseEntity.ok(accountService.blockAccount(accountId));
    }

    // ─────────────────────────────────────────────
    // PATCH /api/v1/accounts/{accountId}/unblock
    // Desbloquear cuenta
    // ─────────────────────────────────────────────
    @PatchMapping("/{accountId}/unblock")
    public ResponseEntity<AccountResponse> unblockAccount(
            @PathVariable Long accountId) {
        log.info("PATCH /api/v1/accounts/{}/unblock", accountId);
        return ResponseEntity.ok(accountService.unblockAccount(accountId));
    }

    // ─────────────────────────────────────────────
    // PATCH /api/v1/accounts/{accountId}/close
    // Cerrar cuenta permanentemente
    // ─────────────────────────────────────────────
    @PatchMapping("/{accountId}/close")
    public ResponseEntity<AccountResponse> closeAccount(
            @PathVariable Long accountId) {
        log.info("PATCH /api/v1/accounts/{}/close", accountId);
        return ResponseEntity.ok(accountService.closeAccount(accountId));
    }
}
