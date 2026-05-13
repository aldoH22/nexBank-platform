package com.nexbank.loan.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nexbank.loan.entity.LoanApplication;
import com.nexbank.loan.enums.LoanStatus;

@Repository
public interface LoanApplicationRepository extends JpaRepository<LoanApplication, Long> {

    List<LoanApplication> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<LoanApplication> findByStatus(LoanStatus status);

    boolean existsByUserIdAndStatusIn(Long userId, List<LoanStatus> statuses);
}