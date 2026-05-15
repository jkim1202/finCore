package org.example.fincore.loan.review.model;

import org.example.fincore.loan.review.entity.LoanReviewDecision;

import java.math.BigDecimal;

/**
 * 대출 심사 정책을 적용한 결과 값입니다.
 * 엔티티 저장 전 승인/거절 판단, 승인 가능 한도, DTI, 적용 정책을 한 번에 전달합니다.
 */
public record LoanReviewEvaluation(
        LoanReviewDecision decision,
        BigDecimal approvedLimit,
        BigDecimal approvedAmount,
        String rejectReason,
        BigDecimal dti,
        LoanReviewPolicy policy
) {
    public static LoanReviewEvaluation approved(
            BigDecimal approvedLimit,
            BigDecimal approvedAmount,
            BigDecimal dti,
            LoanReviewPolicy policy
    ) {
        return new LoanReviewEvaluation(
                LoanReviewDecision.APPROVED,
                approvedLimit,
                approvedAmount,
                null,
                dti,
                policy
        );
    }

    public static LoanReviewEvaluation rejected(
            BigDecimal approvedLimit,
            String rejectReason,
            BigDecimal dti,
            LoanReviewPolicy policy
    ) {
        return new LoanReviewEvaluation(
                LoanReviewDecision.REJECTED,
                approvedLimit,
                null,
                rejectReason,
                dti,
                policy
        );
    }

    public boolean approved() {
        return LoanReviewDecision.APPROVED.equals(decision);
    }
}
