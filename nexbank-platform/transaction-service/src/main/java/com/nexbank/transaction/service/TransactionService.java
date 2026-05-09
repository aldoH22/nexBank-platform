package com.nexbank.transaction.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.nexbank.transaction.client.AccountServiceClient;
import com.nexbank.transaction.dto.AccountResponse;
import com.nexbank.transaction.dto.TransactionRequest;
import com.nexbank.transaction.dto.TransactionResponse;
import com.nexbank.transaction.entity.Transaction;
import com.nexbank.transaction.enums.TransactionStatus;
import com.nexbank.transaction.exception.InsufficientFundsException;
import com.nexbank.transaction.exception.TransactionException;
import com.nexbank.transaction.repository.TransactionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountServiceClient accountServiceClient;

    // ─────────────────────────────────────────────
    // PROCESAR TRANSACCIÓN
    // ─────────────────────────────────────────────

    /**
     * Punto de entrada principal.
     * Valida idempotencia y delega según el tipo.
     */
    public TransactionResponse processTransaction(TransactionRequest request, Long userId) {
        log.info("Procesando transacción: {} tipo: {}", request.getTransactionId(), request.getType());

        // 1. Idempotencia — si ya existe, devolvemos la misma respuesta
        if (transactionRepository.existsByTransactionId(request.getTransactionId())) {
            log.warn("Transacción duplicada detectada: {}", request.getTransactionId());
            return toResponse(transactionRepository
                    .findByTransactionId(request.getTransactionId())
                    .orElseThrow());
        }

        // 2. Delegar según tipo
        return switch (request.getType()) {
            case TRANSFER -> processTransfer(request, userId);
            case DEPOSIT -> processDeposit(request, userId);
            case WITHDRAWAL -> processWithdrawal(request, userId);
        };
    }

    // ─────────────────────────────────────────────
    // TRANSFERENCIA
    // ─────────────────────────────────────────────

    private TransactionResponse processTransfer(TransactionRequest request, Long userId) {
        // Validar campos requeridos
        if (request.getSourceAccountId() == null || request.getTargetAccountId() == null) {
            throw new TransactionException("TRANSFER requiere sourceAccountId y targetAccountId");
        }
        if (request.getSourceAccountId().equals(request.getTargetAccountId())) {
            throw new TransactionException("La cuenta origen y destino no pueden ser la misma");
        }

        // Crear transacción en estado PENDING
        Transaction transaction = buildTransaction(request, userId);
        transaction = transactionRepository.save(transaction);

        try {
            // Validar cuenta origen
            AccountResponse source = accountServiceClient.getAccountById(request.getSourceAccountId());
            if (!"ACTIVE".equals(source.getStatus())) {
                throw new TransactionException("La cuenta origen no está activa");
            }
            if (source.getBalance().compareTo(request.getAmount()) < 0) {
                throw new InsufficientFundsException(request.getSourceAccountId());
            }

            // Validar cuenta destino
            AccountResponse target = accountServiceClient.getAccountById(request.getTargetAccountId());
            if (!"ACTIVE".equals(target.getStatus())) {
                throw new TransactionException("La cuenta destino no está activa");
            }

            // Ejecutar movimientos
            accountServiceClient.withdraw(request.getSourceAccountId(), request.getAmount());
            accountServiceClient.deposit(request.getTargetAccountId(), request.getAmount());

            // Marcar como completada
            transaction.setStatus(TransactionStatus.COMPLETED);
            transaction.setProcessedAt(LocalDateTime.now());
            log.info("Transferencia completada: {}", request.getTransactionId());

        } catch (InsufficientFundsException | TransactionException e) {
            transaction.setStatus(TransactionStatus.FAILED);
            transaction.setErrorMessage(e.getMessage());
            transaction.setProcessedAt(LocalDateTime.now());
            log.error("Transferencia fallida: {} - {}", request.getTransactionId(), e.getMessage());
            transactionRepository.save(transaction);
            throw e;
        } catch (Exception e) {
            transaction.setStatus(TransactionStatus.FAILED);
            transaction.setErrorMessage("Error inesperado: " + e.getMessage());
            transaction.setProcessedAt(LocalDateTime.now());
            log.error("Error inesperado en transferencia: {}", e.getMessage());
            transactionRepository.save(transaction);
            throw new TransactionException("Error al procesar la transferencia");
        }

        return toResponse(transactionRepository.save(transaction));
    }

    // ─────────────────────────────────────────────
    // DEPÓSITO
    // ─────────────────────────────────────────────

    private TransactionResponse processDeposit(TransactionRequest request, Long userId) {
        if (request.getTargetAccountId() == null) {
            throw new TransactionException("DEPOSIT requiere targetAccountId");
        }

        Transaction transaction = buildTransaction(request, userId);
        transaction = transactionRepository.save(transaction);

        try {
            AccountResponse target = accountServiceClient.getAccountById(request.getTargetAccountId());
            if (!"ACTIVE".equals(target.getStatus())) {
                throw new TransactionException("La cuenta destino no está activa");
            }

            accountServiceClient.deposit(request.getTargetAccountId(), request.getAmount());

            transaction.setStatus(TransactionStatus.COMPLETED);
            transaction.setProcessedAt(LocalDateTime.now());
            log.info("Depósito completado: {}", request.getTransactionId());

        } catch (TransactionException e) {
            transaction.setStatus(TransactionStatus.FAILED);
            transaction.setErrorMessage(e.getMessage());
            transaction.setProcessedAt(LocalDateTime.now());
            transactionRepository.save(transaction);
            throw e;
        } catch (Exception e) {
            transaction.setStatus(TransactionStatus.FAILED);
            transaction.setErrorMessage("Error inesperado: " + e.getMessage());
            transaction.setProcessedAt(LocalDateTime.now());
            transactionRepository.save(transaction);
            log.error("Error completo en depósito:", e); // ← cambia esta línea
            throw new TransactionException("Error al procesar el depósito");
        }

        return toResponse(transactionRepository.save(transaction));
    }

    // ─────────────────────────────────────────────
    // RETIRO
    // ─────────────────────────────────────────────

    private TransactionResponse processWithdrawal(TransactionRequest request, Long userId) {
        if (request.getSourceAccountId() == null) {
            throw new TransactionException("WITHDRAWAL requiere sourceAccountId");
        }

        Transaction transaction = buildTransaction(request, userId);
        transaction = transactionRepository.save(transaction);

        try {
            AccountResponse source = accountServiceClient.getAccountById(request.getSourceAccountId());
            if (!"ACTIVE".equals(source.getStatus())) {
                throw new TransactionException("La cuenta origen no está activa");
            }
            if (source.getBalance().compareTo(request.getAmount()) < 0) {
                throw new InsufficientFundsException(request.getSourceAccountId());
            }

            accountServiceClient.withdraw(request.getSourceAccountId(), request.getAmount());

            transaction.setStatus(TransactionStatus.COMPLETED);
            transaction.setProcessedAt(LocalDateTime.now());
            log.info("Retiro completado: {}", request.getTransactionId());

        } catch (InsufficientFundsException | TransactionException e) {
            transaction.setStatus(TransactionStatus.FAILED);
            transaction.setErrorMessage(e.getMessage());
            transaction.setProcessedAt(LocalDateTime.now());
            transactionRepository.save(transaction);
            throw e;
        } catch (Exception e) {
            transaction.setStatus(TransactionStatus.FAILED);
            transaction.setErrorMessage("Error inesperado: " + e.getMessage());
            transaction.setProcessedAt(LocalDateTime.now());
            transactionRepository.save(transaction);
            throw new TransactionException("Error al procesar el retiro");
        }

        return toResponse(transactionRepository.save(transaction));
    }

    // ─────────────────────────────────────────────
    // CONSULTAS
    // ─────────────────────────────────────────────

    public TransactionResponse getByTransactionId(String transactionId) {
        return toResponse(transactionRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new TransactionException(
                        "Transacción no encontrada: " + transactionId)));
    }

    public List<TransactionResponse> getByAccountId(Long accountId) {
        return transactionRepository
                .findBySourceAccountIdOrTargetAccountIdOrderByCreatedAtDesc(accountId, accountId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // ─────────────────────────────────────────────
    // MÉTODOS PRIVADOS
    // ─────────────────────────────────────────────

    private Transaction buildTransaction(TransactionRequest request, Long userId) {
        return Transaction.builder()
                .transactionId(request.getTransactionId())
                .type(request.getType())
                .sourceAccountId(request.getSourceAccountId())
                .targetAccountId(request.getTargetAccountId())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .description(request.getDescription())
                .initiatedBy(userId)
                .build();
    }

    private TransactionResponse toResponse(Transaction t) {
        return TransactionResponse.builder()
                .id(t.getId())
                .transactionId(t.getTransactionId())
                .type(t.getType())
                .status(t.getStatus())
                .sourceAccountId(t.getSourceAccountId())
                .targetAccountId(t.getTargetAccountId())
                .amount(t.getAmount())
                .currency(t.getCurrency())
                .description(t.getDescription())
                .errorMessage(t.getErrorMessage())
                .initiatedBy(t.getInitiatedBy())
                .createdAt(t.getCreatedAt())
                .processedAt(t.getProcessedAt())
                .build();
    }
}
