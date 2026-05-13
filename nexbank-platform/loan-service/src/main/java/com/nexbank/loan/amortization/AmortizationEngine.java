package com.nexbank.loan.amortization;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.nexbank.loan.entity.LoanApplication;
import com.nexbank.loan.entity.LoanPayment;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class AmortizationEngine {

    private static final int SCALE = 2;
    private static final RoundingMode ROUNDING = RoundingMode.HALF_UP;

    /**
     * Genera el plan de pagos completo usando el método francés (cuota fija).
     * Fórmula: M = P * [r(1+r)^n] / [(1+r)^n - 1]
     *   P = capital
     *   r = tasa mensual
     *   n = número de cuotas
     */
    public List<LoanPayment> generatePaymentSchedule(LoanApplication loan) {
        BigDecimal principal    = loan.getRequestedAmount();
        BigDecimal annualRate   = loan.getInterestRate();
        int        termMonths   = loan.getTermMonths();

        // Tasa mensual
        BigDecimal monthlyRate  = annualRate.divide(BigDecimal.valueOf(12), 10, ROUNDING);

        // Cuota mensual fija
        BigDecimal monthlyPayment = calculateMonthlyPayment(principal, monthlyRate, termMonths);

        log.info("Generando {} cuotas de {} para loanId: {}",
                termMonths, monthlyPayment, loan.getId());

        List<LoanPayment> schedule = new ArrayList<>();
        BigDecimal balance         = principal;
        LocalDate  paymentDate     = LocalDate.now().plusMonths(1);

        for (int i = 1; i <= termMonths; i++) {
            // Interés de esta cuota
            BigDecimal interestAmount  = balance.multiply(monthlyRate).setScale(SCALE, ROUNDING);

            // Capital de esta cuota
            BigDecimal principalAmount = monthlyPayment.subtract(interestAmount);

            // Última cuota: ajustar por redondeo
            if (i == termMonths) {
                principalAmount = balance;
                monthlyPayment  = principalAmount.add(interestAmount);
            }

            // Saldo restante
            balance = balance.subtract(principalAmount).setScale(SCALE, ROUNDING);
            if (balance.compareTo(BigDecimal.ZERO) < 0) {
                balance = BigDecimal.ZERO;
            }

            schedule.add(LoanPayment.builder()
                    .loanApplication(loan)
                    .installmentNumber(i)
                    .dueDate(paymentDate)
                    .amount(monthlyPayment.setScale(SCALE, ROUNDING))
                    .principalAmount(principalAmount.setScale(SCALE, ROUNDING))
                    .interestAmount(interestAmount)
                    .remainingBalance(balance)
                    .build());

            paymentDate = paymentDate.plusMonths(1);
        }

        return schedule;
    }

    private BigDecimal calculateMonthlyPayment(BigDecimal principal,
                                               BigDecimal monthlyRate,
                                               int termMonths) {
        // Si tasa = 0, cuota = capital / plazo
        if (monthlyRate.compareTo(BigDecimal.ZERO) == 0) {
            return principal.divide(BigDecimal.valueOf(termMonths), SCALE, ROUNDING);
        }

        // (1 + r)^n
        BigDecimal onePlusR    = BigDecimal.ONE.add(monthlyRate);
        BigDecimal onePlusRtoN = onePlusR.pow(termMonths, new MathContext(10));

        // Numerador: P * r * (1+r)^n
        BigDecimal numerator   = principal.multiply(monthlyRate).multiply(onePlusRtoN);

        // Denominador: (1+r)^n - 1
        BigDecimal denominator = onePlusRtoN.subtract(BigDecimal.ONE);

        return numerator.divide(denominator, SCALE, ROUNDING);
    }

    /**
     * Determina la tasa de interés según tipo y plazo.
     */
    public BigDecimal determineInterestRate(com.nexbank.loan.enums.LoanType loanType, int termMonths) {
        return switch (loanType) {
            case PERSONAL   -> termMonths <= 12
                    ? new BigDecimal("0.2400")   // 24% anual corto plazo
                    : new BigDecimal("0.1800");   // 18% anual largo plazo
            case AUTOMOTIVE -> new BigDecimal("0.1200");  // 12% anual
            case MORTGAGE   -> new BigDecimal("0.0900");  // 9% anual
        };
    }
}
