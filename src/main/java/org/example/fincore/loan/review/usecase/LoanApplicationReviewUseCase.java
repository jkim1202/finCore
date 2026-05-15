package org.example.fincore.loan.review.usecase;

import lombok.RequiredArgsConstructor;
import org.example.fincore.common.exception.BusinessException;
import org.example.fincore.common.exception.ErrorCode;
import org.example.fincore.loan.application.component.LoanApplicationReader;
import org.example.fincore.loan.application.entity.LoanApplication;
import org.example.fincore.loan.review.dto.LoanReviewResponseDto;
import org.example.fincore.loan.review.entity.LoanReview;
import org.example.fincore.loan.review.model.LoanReviewEvaluation;
import org.example.fincore.loan.review.repository.LoanReviewRepository;
import org.example.fincore.loan.review.service.LoanReviewDecisionPolicy;
import org.example.fincore.loan.review.service.LoanReviewRuleSnapshotWriter;
import org.example.fincore.security.FinCoreUserDetails;
import org.example.fincore.user.component.UserReader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LoanApplicationReviewUseCase {
    private final UserReader userReader;
    private final LoanApplicationReader loanApplicationReader;
    private final LoanReviewDecisionPolicy loanReviewDecisionPolicy;
    private final LoanReviewRuleSnapshotWriter loanReviewRuleSnapshotWriter;
    private final LoanReviewRepository loanReviewRepository;

    @Transactional
    public LoanReviewResponseDto reviewLoanApplication(Long applicationId, FinCoreUserDetails userDetails) {
        userReader.getActiveAdmin(userDetails);

        if (loanReviewRepository.existsByApplicationId(applicationId)) {
            throw new BusinessException(ErrorCode.LOAN_REVIEW_ALREADY_EXISTS);
        }

        LoanApplication application = loanApplicationReader.getSubmittedForReview(applicationId);
        LoanReviewEvaluation evaluation = loanReviewDecisionPolicy.evaluate(application);
        String ruleSnapshot = loanReviewRuleSnapshotWriter.write(evaluation);

        LoanReview loanReview;
        if (evaluation.approved()) {
            application.approveReview();
            loanReview = LoanReview.approve(
                    application,
                    evaluation.approvedLimit(),
                    evaluation.approvedAmount(),
                    ruleSnapshot
            );
        } else {
            application.rejectReview();
            loanReview = LoanReview.reject(
                    application,
                    evaluation.approvedLimit(),
                    evaluation.rejectReason(),
                    ruleSnapshot
            );
        }

        return LoanReviewResponseDto.from(loanReviewRepository.save(loanReview));
    }
}
