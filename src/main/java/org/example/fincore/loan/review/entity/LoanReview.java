package org.example.fincore.loan.review.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.fincore.common.exception.BusinessException;
import org.example.fincore.common.exception.ErrorCode;
import org.example.fincore.loan.application.entity.LoanApplication;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "loan_review")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@ToString
@EntityListeners(AuditingEntityListener.class)
public class LoanReview {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long reviewId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false, unique = true)
    private LoanApplication application;

    @Enumerated(EnumType.STRING)
    @Column(name = "decision", nullable = false, length = 20)
    private LoanReviewDecision decision;

    @Column(name = "approved_limit")
    private BigDecimal approvedLimit;

    @Column(name = "approved_amount")
    private BigDecimal approvedAmount;

    @Column(name = "reject_reason", length = 255)
    private String rejectReason;

    @Lob
    @Column(name = "rule_snapshot", columnDefinition = "TEXT")
    private String ruleSnapshot;

    @CreatedDate
    @Column(name = "reviewed_at", nullable = false)
    private LocalDateTime reviewedAt;

    public static LoanReview approve(
            LoanApplication application,
            BigDecimal approvedLimit,
            BigDecimal approvedAmount,
            String ruleSnapshot
    ) {
        validateRequiredAmount(approvedLimit);
        validateRequiredAmount(approvedAmount);

        if (approvedAmount.compareTo(approvedLimit) > 0) {
            throw new BusinessException(ErrorCode.LOAN_REVIEW_INVALID_APPROVED_AMOUNT);
        }

        return LoanReview.builder()
                .application(application)
                .decision(LoanReviewDecision.APPROVED)
                .approvedLimit(approvedLimit)
                .approvedAmount(approvedAmount)
                .rejectReason(null)
                .ruleSnapshot(ruleSnapshot)
                .build();
    }

    public static LoanReview reject(
            LoanApplication application,
            BigDecimal approvedLimit,
            String rejectReason,
            String ruleSnapshot
    ) {
        validateNullableAmount(approvedLimit);

        return LoanReview.builder()
                .application(application)
                .decision(LoanReviewDecision.REJECTED)
                .approvedLimit(approvedLimit)
                .approvedAmount(null)
                .rejectReason(rejectReason)
                .ruleSnapshot(ruleSnapshot)
                .build();
    }

    private static void validateRequiredAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(ErrorCode.LOAN_REVIEW_INVALID_APPROVED_AMOUNT);
        }
    }

    private static void validateNullableAmount(BigDecimal amount) {
        if (amount != null && amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(ErrorCode.LOAN_REVIEW_INVALID_APPROVED_AMOUNT);
        }
    }
}
