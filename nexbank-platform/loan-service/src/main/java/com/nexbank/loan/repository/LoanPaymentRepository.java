package com.nexbank.loan.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nexbank.loan.entity.LoanPayment;
import com.nexbank.loan.enums.PaymentStatus;

@Repository
public interface LoanPaymentRepository extends JpaRepository<LoanPayment, Long> {

    List<LoanPayment> findByLoanApplicationIdOrderByInstallmentNumber(Long loanApplicationId);

    List<LoanPayment> findByStatusAndDueDateBefore(PaymentStatus status, LocalDate date);
}
