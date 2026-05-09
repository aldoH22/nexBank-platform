package com.nexbank.transaction.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nexbank.transaction.entity.Transaction;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // Buscar por transactionId (idempotencia)
    Optional<Transaction> findByTransactionId(String transactionId);

    // Historial de una cuenta (como origen o destino)
    List<Transaction> findBySourceAccountIdOrTargetAccountIdOrderByCreatedAtDesc(
        Long sourceAccountId, Long targetAccountId);

    // Verificar si ya existe una transacción con ese ID
    boolean existsByTransactionId(String transactionId);
}
