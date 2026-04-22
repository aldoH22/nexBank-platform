package com.nexbank.account.repository;

import com.nexbank.account.entity.Account;
import com.nexbank.account.entity.AccountStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad Account.
 */
@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    
    Optional<Account> findByAccountNumber(String accountNumber);
    
    List<Account> findByUserId(Long userId);
    
    List<Account> findByUserIdAndStatus(Long userId, AccountStatus status);
    
    Boolean existsByAccountNumber(String accountNumber);
 
    Long countByUserIdAndStatus(Long userId, AccountStatus status);
}
