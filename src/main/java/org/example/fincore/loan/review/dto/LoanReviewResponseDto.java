package org.example.fincore.loan.review.dto;

import org.example.fincore.loan.application.entity.LoanApplicationStatus;
import org.example.fincore.loan.review.entity.LoanReview;
import org.example.fincore.loan.review.entity.LoanReviewDecision;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record LoanReviewResponseDto(
        Long reviewId,
        Long applicationId,
        LoanReviewDecision decision,
        BigDecimal approvedLimit,
        BigDecimal approvedAmount,
        String rejectReason,
        LoanApplicationStatus applicationStatus,
        LocalDateTime reviewedAt
) {
    public static LoanReviewResponseDto from(LoanReview loanReview) {
        return new LoanReviewResponseDto(
                loanReview.getReviewId(),
                loanReview.getApplication().getApplication_id(),
                loanReview.getDecision(),
                loanReview.getApprovedLimit(),
                loanReview.getApprovedAmount(),
                loanReview.getRejectReason(),
                loanReview.getApplication().getStatus(),
                loanReview.getReviewedAt()
        );
    }
}
