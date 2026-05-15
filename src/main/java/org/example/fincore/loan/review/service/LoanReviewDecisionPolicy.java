package org.example.fincore.loan.review.service;

import lombok.RequiredArgsConstructor;
import org.example.fincore.loan.application.entity.LoanApplication;
import org.example.fincore.loan.review.model.LoanReviewEvaluation;
import org.example.fincore.loan.review.model.LoanReviewPolicy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class LoanReviewDecisionPolicy {
    private final DtiCalculator dtiCalculator;
    private final CreditLimitCalculator creditLimitCalculator;

    /**
     * 심사 정책은 명확한 거절 사유를 남기기 위해 순서대로 평가합니다.
     * 여러 조건에 동시에 걸리더라도 먼저 실패한 정책의 사유가 최종 거절 사유가 됩니다.
     */
    public LoanReviewEvaluation evaluate(LoanApplication application) {
        LoanReviewPolicy policy = LoanReviewPolicy.defaultPolicy();
        BigDecimal dti = dtiCalculator.calculate(application.getAnnualIncome(), application.getExistingDebtAmount());
        BigDecimal approvedLimit = creditLimitCalculator.calculate(application, policy);

        // 상환 능력 판단 전에 최소 소득 기준을 먼저 확인합니다.
        if (application.getAnnualIncome().compareTo(policy.minimumAnnualIncome()) < 0) {
            return LoanReviewEvaluation.rejected(approvedLimit, "MINIMUM_INCOME_NOT_MET", dti, policy);
        }

        // DTI가 정책 한도를 넘으면 기존 부채 부담이 크다고 보고 거절합니다.
        if (dti.compareTo(policy.maxDtiRatio()) > 0) {
            return LoanReviewEvaluation.rejected(approvedLimit, "DTI_EXCEEDED", dti, policy);
        }

        // 신청 금액이 산정 한도보다 크면 승인 가능 한도 밖의 요청이므로 거절합니다.
        if (application.getRequestedAmount().compareTo(approvedLimit) > 0) {
            return LoanReviewEvaluation.rejected(approvedLimit, "REQUESTED_AMOUNT_EXCEEDS_LIMIT", dti, policy);
        }

        return LoanReviewEvaluation.approved(
                approvedLimit,
                application.getRequestedAmount(),
                dti,
                policy
        );
    }
}
