package com.nexbank.loan.scoring;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Component;

import com.nexbank.loan.dto.LoanRequest;
import com.nexbank.loan.entity.CreditScoring;
import com.nexbank.loan.entity.LoanApplication;
import com.nexbank.loan.enums.LoanType;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class CreditScoringEngine {

    // Umbrales de decisión
    private static final int SCORE_AUTO_APPROVE  = 650;
    private static final int SCORE_MANUAL_REVIEW = 500;
    // < 500 → REJECTED automático

    // Pesos de cada criterio (suman 100)
    private static final int WEIGHT_INCOME       = 30;
    private static final int WEIGHT_DEBT_RATIO   = 35;
    private static final int WEIGHT_LOAN_AMOUNT  = 25;
    private static final int WEIGHT_LOAN_TYPE    = 10;

    /**
     * Ejecuta el motor de scoring y devuelve la entidad lista para persistir.
     */
    public CreditScoring evaluate(LoanRequest request, LoanApplication loan) {
        log.info("Evaluando scoring para userId: {}", request.getUserId());

        // 1. Puntaje por ingresos mensuales (0–100)
        int incomeScore = evaluateIncome(request.getMonthlyIncome());

        // 2. Puntaje por ratio deuda/ingreso (0–100)
        BigDecimal dti = calculateDebtToIncomeRatio(request.getMonthlyDebts(), request.getMonthlyIncome());
        int debtRatioScore = evaluateDebtRatio(dti);

        // 3. Puntaje por monto solicitado vs ingresos anuales (0–100)
        int loanAmountScore = evaluateLoanAmount(request.getRequestedAmount(), request.getMonthlyIncome());

        // 4. Puntaje por tipo de préstamo (0–100)
        int loanTypeScore = evaluateLoanType(request.getLoanType());

        // Score ponderado: escala 0–100
        int weightedScore = (incomeScore    * WEIGHT_INCOME      / 100)
                          + (debtRatioScore * WEIGHT_DEBT_RATIO  / 100)
                          + (loanAmountScore * WEIGHT_LOAN_AMOUNT / 100)
                          + (loanTypeScore  * WEIGHT_LOAN_TYPE   / 100);

        // Mapear a rango 300–850 (igual que score crediticio real)
        int finalScore = 300 + (weightedScore * 550 / 100);

        // Decisión
        String decision;
        String reason;

        if (finalScore >= SCORE_AUTO_APPROVE) {
            decision = "APPROVED";
            reason   = buildApprovedReason(finalScore, dti);
        } else if (finalScore >= SCORE_MANUAL_REVIEW) {
            decision = "MANUAL_REVIEW";
            reason   = buildReviewReason(finalScore, incomeScore, debtRatioScore);
        } else {
            decision = "REJECTED";
            reason   = buildRejectedReason(finalScore, dti, incomeScore);
        }

        log.info("Scoring resultado: score={} decisión={} para userId={}", finalScore, decision, request.getUserId());

        return CreditScoring.builder()
                .userId(request.getUserId())
                .loanApplication(loan)
                .finalScore(finalScore)
                .incomeScore(incomeScore)
                .debtRatioScore(debtRatioScore)
                .loanAmountScore(loanAmountScore)
                .loanTypeScore(loanTypeScore)
                .debtToIncomeRatio(dti)
                .decision(decision)
                .decisionReason(reason)
                .build();
    }

    // ─── Criterio 1: Ingresos ───────────────────────────────────────────────────

    private int evaluateIncome(BigDecimal monthlyIncome) {
        double income = monthlyIncome.doubleValue();
        if (income >= 50_000) return 100;
        if (income >= 30_000) return 85;
        if (income >= 15_000) return 65;
        if (income >= 8_000)  return 45;
        if (income >= 4_000)  return 25;
        return 10;
    }

    // ─── Criterio 2: Ratio deuda/ingreso (DTI) ─────────────────────────────────

    private BigDecimal calculateDebtToIncomeRatio(BigDecimal debts, BigDecimal income) {
        if (income.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ONE;
        return debts.divide(income, 4, RoundingMode.HALF_UP);
    }

    private int evaluateDebtRatio(BigDecimal dti) {
        double ratio = dti.doubleValue();
        if (ratio <= 0.10) return 100;  // Deuda < 10% del ingreso → excelente
        if (ratio <= 0.20) return 85;
        if (ratio <= 0.30) return 70;
        if (ratio <= 0.40) return 50;
        if (ratio <= 0.50) return 30;
        return 10;                       // Deuda > 50% del ingreso → muy alto riesgo
    }

    // ─── Criterio 3: Monto solicitado vs ingresos anuales ──────────────────────

    private int evaluateLoanAmount(BigDecimal requestedAmount, BigDecimal monthlyIncome) {
        BigDecimal annualIncome = monthlyIncome.multiply(BigDecimal.valueOf(12));
        if (annualIncome.compareTo(BigDecimal.ZERO) == 0) return 0;

        double ratio = requestedAmount.divide(annualIncome, 4, RoundingMode.HALF_UP).doubleValue();

        if (ratio <= 1.0)  return 100;  // Pide menos de 1 año de ingresos → excelente
        if (ratio <= 2.0)  return 80;
        if (ratio <= 3.0)  return 60;
        if (ratio <= 5.0)  return 40;
        if (ratio <= 8.0)  return 20;
        return 5;                        // Pide más de 8× su ingreso anual → muy riesgoso
    }

    // ─── Criterio 4: Tipo de préstamo ──────────────────────────────────────────

    private int evaluateLoanType(LoanType loanType) {
        return switch (loanType) {
            case PERSONAL   -> 70;   // Mayor riesgo — sin garantía
            case AUTOMOTIVE -> 85;   // Riesgo medio — auto como garantía
            case MORTGAGE   -> 95;   // Menor riesgo — inmueble como garantía
        };
    }

    // ─── Builders de razones ───────────────────────────────────────────────────

    private String buildApprovedReason(int score, BigDecimal dti) {
        return String.format(
            "Solicitud aprobada automáticamente. Score: %d. Ratio deuda/ingreso: %.1f%%.",
            score, dti.doubleValue() * 100
        );
    }

    private String buildReviewReason(int score, int incomeScore, int debtRatioScore) {
        return String.format(
            "Score borderline (%d). Requiere revisión manual. Ingresos: %d/100. DTI: %d/100.",
            score, incomeScore, debtRatioScore
        );
    }

    private String buildRejectedReason(int score, BigDecimal dti, int incomeScore) {
        return String.format(
            "Solicitud rechazada. Score insuficiente: %d. Ratio deuda/ingreso: %.1f%%. Ingresos insuficientes: %d/100.",
            score, dti.doubleValue() * 100, incomeScore
        );
    }
}
