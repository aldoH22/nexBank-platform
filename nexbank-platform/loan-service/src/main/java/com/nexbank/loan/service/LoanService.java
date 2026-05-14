package com.nexbank.loan.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.nexbank.common.event.LoanEvent;
import com.nexbank.loan.amortization.AmortizationEngine;
import com.nexbank.loan.client.AccountServiceClient;
import com.nexbank.loan.dto.AccountResponse;
import com.nexbank.loan.dto.LoanPaymentResponse;
import com.nexbank.loan.dto.LoanRequest;
import com.nexbank.loan.dto.LoanResponse;
import com.nexbank.loan.dto.PayLoanInstallmentRequest;
import com.nexbank.loan.entity.CreditScoring;
import com.nexbank.loan.entity.LoanApplication;
import com.nexbank.loan.entity.LoanPayment;
import com.nexbank.loan.enums.LoanStatus;
import com.nexbank.loan.enums.PaymentStatus;
import com.nexbank.loan.exception.ActiveLoanExistsException;
import com.nexbank.loan.exception.LoanNotApprovedException;
import com.nexbank.loan.exception.LoanNotFoundException;
import com.nexbank.loan.kafka.LoanEventPublisher;
import com.nexbank.loan.repository.CreditScoringRepository;
import com.nexbank.loan.repository.LoanApplicationRepository;
import com.nexbank.loan.repository.LoanPaymentRepository;
import com.nexbank.loan.scoring.CreditScoringEngine;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoanService {

    private final LoanApplicationRepository loanRepository;
    private final LoanPaymentRepository     paymentRepository;
    private final CreditScoringRepository   scoringRepository;
    private final CreditScoringEngine       scoringEngine;
    private final AmortizationEngine        amortizationEngine;
    private final AccountServiceClient      accountServiceClient;
    private final LoanEventPublisher        eventPublisher;

    // ─────────────────────────────────────────────────────────────────────────
    // SOLICITAR PRÉSTAMO
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional
    public LoanResponse applyForLoan(LoanRequest request) {
        log.info("Nueva solicitud de préstamo - userId: {} tipo: {} monto: {}",
                request.getUserId(), request.getLoanType(), request.getRequestedAmount());

        // 1. Validar que no tenga préstamo activo o en revisión
        boolean hasActiveLoan = loanRepository.existsByUserIdAndStatusIn(
                request.getUserId(),
                List.of(LoanStatus.ACTIVE, LoanStatus.UNDER_REVIEW, LoanStatus.PENDING)
        );
        if (hasActiveLoan) {
            throw new ActiveLoanExistsException(request.getUserId());
        }

        // 2. Validar que la cuenta de desembolso existe y pertenece al usuario
        AccountResponse account = accountServiceClient.getAccountById(request.getDisbursementAccountId());
        if (!account.getUserId().equals(request.getUserId())) {
            throw new LoanNotApprovedException("La cuenta de desembolso no pertenece al usuario");
        }
        if (!"ACTIVE".equals(account.getStatus())) {
            throw new LoanNotApprovedException("La cuenta de desembolso no está activa");
        }

        // 3. Determinar tasa de interés según tipo y plazo
        BigDecimal interestRate = amortizationEngine.determineInterestRate(
                request.getLoanType(), request.getTermMonths());

        // 4. Crear solicitud en estado PENDING
        LoanApplication loan = LoanApplication.builder()
                .userId(request.getUserId())
                .disbursementAccountId(request.getDisbursementAccountId())
                .loanType(request.getLoanType())
                .requestedAmount(request.getRequestedAmount())
                .interestRate(interestRate)
                .termMonths(request.getTermMonths())
                .monthlyIncome(request.getMonthlyIncome())
                .monthlyDebts(request.getMonthlyDebts())
                .purpose(request.getPurpose())
                .build();

        loan = loanRepository.save(loan);

        // 5. Ejecutar motor de scoring
        CreditScoring scoring = scoringEngine.evaluate(request, loan);
        scoring = scoringRepository.save(scoring);

        // 6. Actualizar préstamo según decisión del scoring
        loan.setCreditScore(scoring.getFinalScore());

        switch (scoring.getDecision()) {
            case "APPROVED" -> {
                loan.setStatus(LoanStatus.APPROVED);
                loan.setApprovedAt(LocalDateTime.now());

                // Calcular cuota mensual y monto total
                BigDecimal monthlyPayment = calculateMonthlyPaymentPreview(
                        request.getRequestedAmount(), interestRate, request.getTermMonths());
                loan.setMonthlyPayment(monthlyPayment);
                loan.setTotalAmount(monthlyPayment.multiply(
                        BigDecimal.valueOf(request.getTermMonths())));

                log.info("Préstamo APROBADO - userId: {} score: {}", request.getUserId(), scoring.getFinalScore());
            }
            case "MANUAL_REVIEW" -> {
                loan.setStatus(LoanStatus.UNDER_REVIEW);
                log.info("Préstamo en REVISIÓN MANUAL - userId: {} score: {}", request.getUserId(), scoring.getFinalScore());
            }
            case "REJECTED" -> {
                loan.setStatus(LoanStatus.REJECTED);
                loan.setRejectionReason(scoring.getDecisionReason());
                loan.setRejectedAt(LocalDateTime.now());
                log.info("Préstamo RECHAZADO - userId: {} score: {}", request.getUserId(), scoring.getFinalScore());
            }
        }

        loan = loanRepository.save(loan);
        // Publicar evento según decisión
        eventPublisher.publish(buildEvent(loan));
        return toResponse(loan);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DESEMBOLSAR PRÉSTAMO (activa el préstamo y genera el plan de pagos)
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional
    public LoanResponse disburseLoan(Long loanId) {
        log.info("Desembolsando préstamo id: {}", loanId);

        LoanApplication loan = findLoanById(loanId);

        if (loan.getStatus() != LoanStatus.APPROVED) {
            throw new LoanNotApprovedException(
                "El préstamo debe estar en estado APPROVED para desembolsarse. Estado actual: " + loan.getStatus());
        }

        // Generar plan de amortización
        List<LoanPayment> schedule = amortizationEngine.generatePaymentSchedule(loan);
        paymentRepository.saveAll(schedule);

        loan.setStatus(LoanStatus.ACTIVE);
        loan = loanRepository.save(loan);

        eventPublisher.publish(buildEvent(loan));

        log.info("Préstamo desembolsado. {} cuotas generadas para loanId: {}", schedule.size(), loanId);
        return toResponse(loan);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PAGAR CUOTA
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional
    public LoanPaymentResponse payInstallment(Long loanId, PayLoanInstallmentRequest request) {
        log.info("Pago de cuota - loanId: {} paymentId: {}", loanId, request.getPaymentId());

        LoanPayment payment = paymentRepository.findById(request.getPaymentId())
                .orElseThrow(() -> new LoanNotFoundException(request.getPaymentId()));

        if (payment.getStatus() == PaymentStatus.PAID) {
            throw new LoanNotApprovedException("Esta cuota ya fue pagada");
        }

        LoanApplication loan = payment.getLoanApplication();
        if (!loan.getId().equals(loanId)) {
            throw new LoanNotFoundException(loanId);
        }
        if (loan.getStatus() != LoanStatus.ACTIVE) {
            throw new LoanNotApprovedException("El préstamo no está activo");
        }

        // Marcar cuota como pagada
        payment.setStatus(PaymentStatus.PAID);
        payment.setPaidAt(LocalDateTime.now());
        payment = paymentRepository.save(payment);

        // Verificar si todas las cuotas están pagadas → cerrar préstamo
        long pendingPayments = paymentRepository
                .findByLoanApplicationIdOrderByInstallmentNumber(loanId)
                .stream()
                .filter(p -> p.getStatus() != PaymentStatus.PAID)
                .count();

        if (pendingPayments == 0) {
            loan.setStatus(LoanStatus.PAID);
            loanRepository.save(loan);
            eventPublisher.publish(buildEvent(loan));
            log.info("Préstamo {} completamente pagado", loanId);
        }

        return toPaymentResponse(payment);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CONSULTAS
    // ─────────────────────────────────────────────────────────────────────────

    public LoanResponse getLoanById(Long loanId) {
        return toResponse(findLoanById(loanId));
    }

    public List<LoanResponse> getLoansByUserId(Long userId) {
        return loanRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<LoanPaymentResponse> getPaymentSchedule(Long loanId) {
        findLoanById(loanId); // Valida que existe
        return paymentRepository.findByLoanApplicationIdOrderByInstallmentNumber(loanId)
                .stream()
                .map(this::toPaymentResponse)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Metodos privados
    // ─────────────────────────────────────────────────────────────────────────

    private LoanEvent buildEvent(LoanApplication loan) {
        return LoanEvent.builder()
                .loanId(loan.getId())
                .userId(loan.getUserId())
                .loanType(loan.getLoanType().name())
                .status(loan.getStatus().name())
                .requestedAmount(loan.getRequestedAmount())
                .monthlyPayment(loan.getMonthlyPayment())
                .creditScore(loan.getCreditScore())
                .rejectionReason(loan.getRejectionReason())
                .occurredAt(LocalDateTime.now())
                .build();
    }

    private LoanApplication findLoanById(Long loanId) {
        return loanRepository.findById(loanId)
                .orElseThrow(() -> new LoanNotFoundException(loanId));
    }

    private BigDecimal calculateMonthlyPaymentPreview(BigDecimal principal,
                                                      BigDecimal annualRate,
                                                      int termMonths) {
        // Reutiliza la lógica del motor, pero sin persistir
        LoanApplication temp = LoanApplication.builder()
                .requestedAmount(principal)
                .interestRate(annualRate)
                .termMonths(termMonths)
                .build();
        // Devuelve la primera cuota que sería representativa
        return amortizationEngine.generatePaymentSchedule(temp)
                .get(0)
                .getAmount();
    }

    private LoanResponse toResponse(LoanApplication loan) {
        return LoanResponse.builder()
                .id(loan.getId())
                .userId(loan.getUserId())
                .disbursementAccountId(loan.getDisbursementAccountId())
                .loanType(loan.getLoanType())
                .status(loan.getStatus())
                .requestedAmount(loan.getRequestedAmount())
                .interestRate(loan.getInterestRate())
                .termMonths(loan.getTermMonths())
                .monthlyPayment(loan.getMonthlyPayment())
                .totalAmount(loan.getTotalAmount())
                .purpose(loan.getPurpose())
                .rejectionReason(loan.getRejectionReason())
                .creditScore(loan.getCreditScore())
                .monthlyIncome(loan.getMonthlyIncome())
                .monthlyDebts(loan.getMonthlyDebts())
                .createdAt(loan.getCreatedAt())
                .approvedAt(loan.getApprovedAt())
                .rejectedAt(loan.getRejectedAt())
                .build();
    }

    private LoanPaymentResponse toPaymentResponse(LoanPayment payment) {
        return LoanPaymentResponse.builder()
                .id(payment.getId())
                .loanApplicationId(payment.getLoanApplication().getId())
                .installmentNumber(payment.getInstallmentNumber())
                .dueDate(payment.getDueDate())
                .amount(payment.getAmount())
                .principalAmount(payment.getPrincipalAmount())
                .interestAmount(payment.getInterestAmount())
                .remainingBalance(payment.getRemainingBalance())
                .status(payment.getStatus())
                .paidAt(payment.getPaidAt())
                .build();
    }
}
