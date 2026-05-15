package org.example.fincore.loan.review.service;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class DtiCalculator {
    public BigDecimal calculate(BigDecimal annualIncome, BigDecimal existingDebtAmount) {
        // 소득이 없거나 비정상 값이면 심사에서 통과되지 않도록 DTI를 100%로 간주합니다.
        if (annualIncome == null || annualIncome.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ONE;
        }

        // DTI = 기존 부채 / 연소득. 소수점 넷째 자리까지 보관해 정책 비교에 사용합니다.
        BigDecimal debtAmount = existingDebtAmount == null ? BigDecimal.ZERO : existingDebtAmount;
        return debtAmount.divide(annualIncome, 4, RoundingMode.HALF_UP);
    }
}
