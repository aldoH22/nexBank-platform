package com.nexbank.transaction.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nexbank.transaction.dto.TransactionRequest;
import com.nexbank.transaction.dto.TransactionResponse;
import com.nexbank.transaction.service.TransactionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
@Slf4j
public class TransactionController {

    private final TransactionService transactionService;

    // ─────────────────────────────────────────────
    // POST /api/v1/transactions
    // Procesar una transacción (transfer, deposit, withdrawal)
    // ─────────────────────────────────────────────
    @PostMapping
    public ResponseEntity<TransactionResponse> processTransaction(
            @Valid @RequestBody TransactionRequest request,
            @RequestHeader("X-User-Id") Long userId) {
        log.info("POST /api/v1/transactions - tipo: {} transactionId: {}",
                request.getType(), request.getTransactionId());
        TransactionResponse response = transactionService.processTransaction(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ─────────────────────────────────────────────
    // GET /api/v1/transactions/{transactionId}
    // Obtener transacción por su ID único
    // ─────────────────────────────────────────────
    @GetMapping("/{transactionId}")
    public ResponseEntity<TransactionResponse> getByTransactionId(
            @PathVariable String transactionId) {
        log.info("GET /api/v1/transactions/{}", transactionId);
        return ResponseEntity.ok(transactionService.getByTransactionId(transactionId));
    }

    // ─────────────────────────────────────────────
    // GET /api/v1/transactions/account/{accountId}
    // Obtener historial de transacciones de una cuenta
    // ─────────────────────────────────────────────
    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<TransactionResponse>> getByAccountId(
            @PathVariable Long accountId) {
        log.info("GET /api/v1/transactions/account/{}", accountId);
        return ResponseEntity.ok(transactionService.getByAccountId(accountId));
    }
}
