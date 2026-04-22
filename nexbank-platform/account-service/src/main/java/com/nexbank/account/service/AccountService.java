package com.nexbank.account.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.nexbank.account.client.AuthServiceClient;
import com.nexbank.account.dto.AccountResponse;
import com.nexbank.account.dto.CreateAccountRequest;
import com.nexbank.account.dto.UpdateAccountRequest;
import com.nexbank.account.dto.UserResponse;
import com.nexbank.account.entity.Account;
import com.nexbank.account.entity.AccountStatus;
import com.nexbank.account.exception.AccountNotActiveException;
import com.nexbank.account.exception.AccountNotFoundException;
import com.nexbank.account.exception.UserNotFoundException;
import com.nexbank.account.repository.AccountRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountService {

    private final AccountRepository accountRepository;
    private final AuthServiceClient authServiceClient;

    // ─────────────────────────────────────────────
    // CREAR CUENTA
    // ─────────────────────────────────────────────

    /**
     * Crea una nueva cuenta bancaria.
     * Valida que el usuario exista en auth-service antes de crear.
     */
    public AccountResponse createAccount(CreateAccountRequest request) {
        log.info("Creando cuenta para userId: {}", request.getUserId());

        // 1. Validar que el usuario existe en auth-service
        UserResponse user = getUserOrThrow(request.getUserId());

        // 2. Generar número de cuenta único
        String accountNumber = generateUniqueAccountNumber();

        // 3. Construir y guardar la entidad
        Account account = Account.builder()
                .accountNumber(accountNumber)
                .userId(request.getUserId())
                .accountType(request.getAccountType())
                .currency(request.getCurrency() != null ? request.getCurrency() : "MXN")
                .alias(request.getAlias())
                .dailyTransactionLimit(request.getDailyTransactionLimit())
                .build();

        Account saved = accountRepository.save(account);
        log.info("Cuenta creada exitosamente: {}", saved.getAccountNumber());

        return toResponse(saved, user);
    }

    // ─────────────────────────────────────────────
    // CONSULTAR CUENTAS
    // ─────────────────────────────────────────────

    /**
     * Obtiene una cuenta por su ID.
     */
    public AccountResponse getAccountById(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(
                        "Cuenta no encontrada con ID: " + accountId));

        UserResponse user = getUserOrThrow(account.getUserId());
        return toResponse(account, user);
    }

    /**
     * Obtiene una cuenta por su número de cuenta.
     */
    public AccountResponse getAccountByNumber(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(
                        "Cuenta no encontrada: " + accountNumber));

        UserResponse user = getUserOrThrow(account.getUserId());
        return toResponse(account, user);
    }

    /**
     * Obtiene todas las cuentas de un usuario.
     */
    public List<AccountResponse> getAccountsByUserId(Long userId) {
        log.info("Buscando cuentas para userId: {}", userId);

        // Validar que el usuario existe
        UserResponse user = getUserOrThrow(userId);

        List<Account> accounts = accountRepository.findByUserId(userId);
        return accounts.stream()
                .map(account -> toResponse(account, user))
                .toList();
    }

    // ─────────────────────────────────────────────
    // ACTUALIZAR CUENTA
    // ─────────────────────────────────────────────

    /**
     * Actualiza alias y/o límite diario de una cuenta.
     */
    public AccountResponse updateAccount(Long accountId, UpdateAccountRequest request) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(
                        "Cuenta no encontrada con ID: " + accountId));

        if (!account.isActive()) {
            throw new AccountNotActiveException(
                    "No se puede modificar una cuenta inactiva: " + accountId);
        }

        // Solo actualizamos los campos que vienen en el request
        if (request.getAlias() != null) {
            account.setAlias(request.getAlias());
        }
        if (request.getDailyTransactionLimit() != null) {
            account.setDailyTransactionLimit(request.getDailyTransactionLimit());
        }

        Account updated = accountRepository.save(account);
        UserResponse user = getUserOrThrow(account.getUserId());
        return toResponse(updated, user);
    }

    // ─────────────────────────────────────────────
    // CAMBIAR ESTADO
    // ─────────────────────────────────────────────

    /**
     * Bloquea una cuenta activa.
     */
    public AccountResponse blockAccount(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(
                        "Cuenta no encontrada con ID: " + accountId));

        if (account.getStatus() == AccountStatus.BLOCKED) {
            throw new AccountNotActiveException("La cuenta ya está bloqueada");
        }
        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new AccountNotActiveException("No se puede bloquear una cuenta cerrada");
        }

        account.setStatus(AccountStatus.BLOCKED);
        Account updated = accountRepository.save(account);
        log.info("Cuenta bloqueada: {}", accountId);

        UserResponse user = getUserOrThrow(account.getUserId());
        return toResponse(updated, user);
    }

    /**
     * Desbloquea una cuenta bloqueada.
     */
    public AccountResponse unblockAccount(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(
                        "Cuenta no encontrada con ID: " + accountId));

        if (account.getStatus() != AccountStatus.BLOCKED) {
            throw new AccountNotActiveException(
                    "Solo se pueden desbloquear cuentas bloqueadas");
        }

        account.setStatus(AccountStatus.ACTIVE);
        Account updated = accountRepository.save(account);
        log.info("Cuenta desbloqueada: {}", accountId);

        UserResponse user = getUserOrThrow(account.getUserId());
        return toResponse(updated, user);
    }

    /**
     * Cierra una cuenta permanentemente.
     */
    public AccountResponse closeAccount(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(
                        "Cuenta no encontrada con ID: " + accountId));

        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new AccountNotActiveException("La cuenta ya está cerrada");
        }
        if (account.getBalance().compareTo(BigDecimal.ZERO) > 0) {
            throw new AccountNotActiveException(
                    "No se puede cerrar una cuenta con saldo positivo");
        }

        account.setStatus(AccountStatus.CLOSED);
        account.setClosedAt(LocalDateTime.now());
        Account updated = accountRepository.save(account);
        log.info("Cuenta cerrada: {}", accountId);

        UserResponse user = getUserOrThrow(account.getUserId());
        return toResponse(updated, user);
    }

    // ─────────────────────────────────────────────
    // MÉTODOS PRIVADOS
    // ─────────────────────────────────────────────

    /**
     * Consulta el usuario en auth-service.
     * Si Feign lanza excepción, la convertimos a una propia.
     */
    private UserResponse getUserOrThrow(Long userId) {
        try {
            return authServiceClient.getUserById(userId);
        } catch (Exception e) {
            log.error("Error al consultar auth-service, userId: {}, error: {}, mensaje: {}",
                    userId, e.getClass().getName(), e.getMessage());
            throw new UserNotFoundException("Usuario no encontrado con ID: " + userId);
        }
    }

    /**
     * Genera un número de cuenta único de 10 dígitos.
     * Reintenta si ya existe uno igual (colisión muy improbable).
     */
    private String generateUniqueAccountNumber() {
        String accountNumber;
        do {
            accountNumber = String.format("%010d",
                    (long) (Math.random() * 9_000_000_000L) + 1_000_000_000L);
        } while (accountRepository.existsByAccountNumber(accountNumber));
        return accountNumber;
    }

    /**
     * Convierte Account + UserResponse a AccountResponse.
     */
    private AccountResponse toResponse(Account account, UserResponse user) {
        return AccountResponse.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .userId(account.getUserId())
                .username(user.getUsername())
                .accountType(account.getAccountType())
                .balance(account.getBalance())
                .currency(account.getCurrency())
                .status(account.getStatus())
                .dailyTransactionLimit(account.getDailyTransactionLimit())
                .alias(account.getAlias())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .build();
    }
}
