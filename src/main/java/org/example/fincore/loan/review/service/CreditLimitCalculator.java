package org.example.fincore.loan.review.service;

import org.example.fincore.loan.application.entity.LoanApplication;
import org.example.fincore.loan.review.model.LoanReviewPolicy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class CreditLimitCalculator {
    public BigDecimal calculate(LoanApplication application, LoanReviewPolicy policy) {
        // 고객 소득 기준 한도입니다. MVP에서는 연소득에 정책 배수를 곱해 단순 산정합니다.
        BigDecimal incomeBasedLimit = application.getAnnualIncome()
                .multiply(policy.incomeMultiplier());

        // 상품 기준 한도입니다. 상품 최대 금액에 정책 비율을 적용해 상품별 상한을 넘지 않게 합니다.
        BigDecimal productLimit = application.getLoanProduct().getMaxAmount()
                .multiply(policy.maxLimitRatioOfProductMaxAmount());

        // 실제 승인 가능 한도는 소득 기준 한도와 상품 기준 한도 중 더 작은 값입니다.
        return incomeBasedLimit.min(productLimit);
    }
}
