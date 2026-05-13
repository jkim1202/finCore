package org.example.fincore.loan.application.dto;

import org.example.fincore.loan.application.entity.LoanApplication;
import org.example.fincore.loan.application.entity.LoanApplicationStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record LoanApplicationSearchResponseDto(
        Long applicationId,
        Long userId,
        String userName,
        Long loanProductId,
        String loanProductName,
        String disbursementAccountNumber,
        BigDecimal requestedAmount,
        Integer requestedTermMonths,
        BigDecimal annualIncome,
        LoanApplicationStatus status,
        LocalDateTime submittedAt,
        LocalDateTime reviewedAt
) {
    public static LoanApplicationSearchResponseDto from(LoanApplication loanApplication) {
        return new LoanApplicationSearchResponseDto(
                loanApplication.getApplication_id(),
                loanApplication.getUser().getId(),
                loanApplication.getUser().getName(),
                loanApplication.getLoanProduct().getLoanProductId(),
                loanApplication.getLoanProduct().getProductName(),
                loanApplication.getDisbursementAccount().getAccountNumber(),
                loanApplication.getRequestedAmount(),
                loanApplication.getRequestedTermMonths(),
                loanApplication.getAnnualIncome(),
                loanApplication.getStatus(),
                loanApplication.getSubmittedAt(),
                loanApplication.getReviewedAt()
        );
    }
}
