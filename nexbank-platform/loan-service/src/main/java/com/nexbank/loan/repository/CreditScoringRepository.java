package com.nexbank.loan.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nexbank.loan.entity.CreditScoring;

@Repository
public interface CreditScoringRepository extends JpaRepository<CreditScoring, Long> {

    Optional<CreditScoring> findByLoanApplicationId(Long loanApplicationId);
}
