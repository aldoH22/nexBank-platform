package com.nexbank.loan.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nexbank.loan.dto.LoanPaymentResponse;
import com.nexbank.loan.dto.LoanRequest;
import com.nexbank.loan.dto.LoanResponse;
import com.nexbank.loan.dto.PayLoanInstallmentRequest;
import com.nexbank.loan.service.LoanService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/loans")
@RequiredArgsConstructor
@Slf4j
public class LoanController {

    private final LoanService loanService;

    // POST /api/v1/loans — Solicitar préstamo
    @PostMapping
    public ResponseEntity<LoanResponse> applyForLoan(
            @Valid @RequestBody LoanRequest request) {
        log.info("POST /api/v1/loans - userId: {} tipo: {}", request.getUserId(), request.getLoanType());
        return ResponseEntity.status(HttpStatus.CREATED).body(loanService.applyForLoan(request));
    }

    // POST /api/v1/loans/{loanId}/disburse — Desembolsar préstamo aprobado
    @PostMapping("/{loanId}/disburse")
    public ResponseEntity<LoanResponse> disburseLoan(
            @PathVariable Long loanId) {
        log.info("POST /api/v1/loans/{}/disburse", loanId);
        return ResponseEntity.ok(loanService.disburseLoan(loanId));
    }

    // POST /api/v1/loans/{loanId}/pay — Pagar una cuota
    @PostMapping("/{loanId}/pay")
    public ResponseEntity<LoanPaymentResponse> payInstallment(
            @PathVariable Long loanId,
            @Valid @RequestBody PayLoanInstallmentRequest request) {
        log.info("POST /api/v1/loans/{}/pay - paymentId: {}", loanId, request.getPaymentId());
        return ResponseEntity.ok(loanService.payInstallment(loanId, request));
    }

    // GET /api/v1/loans/{loanId} — Consultar préstamo
    @GetMapping("/{loanId}")
    public ResponseEntity<LoanResponse> getLoanById(@PathVariable Long loanId) {
        log.info("GET /api/v1/loans/{}", loanId);
        return ResponseEntity.ok(loanService.getLoanById(loanId));
    }

    // GET /api/v1/loans/user/{userId} — Préstamos de un usuario
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<LoanResponse>> getLoansByUserId(@PathVariable Long userId) {
        log.info("GET /api/v1/loans/user/{}", userId);
        return ResponseEntity.ok(loanService.getLoansByUserId(userId));
    }

    // GET /api/v1/loans/{loanId}/schedule — Plan de pagos
    @GetMapping("/{loanId}/schedule")
    public ResponseEntity<List<LoanPaymentResponse>> getPaymentSchedule(@PathVariable Long loanId) {
        log.info("GET /api/v1/loans/{}/schedule", loanId);
        return ResponseEntity.ok(loanService.getPaymentSchedule(loanId));
    }
}
