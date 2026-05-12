package org.example.fincore.loan.application.dto;

import org.example.fincore.loan.application.entity.LoanApplication;
import org.example.fincore.loan.application.entity.LoanApplicationStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record LoanApplicationResponseDto(
        String userName,
        String loanProductName,
        String disbursementAccountNumber,
        BigDecimal requestedAmount,
        Integer requestedTermMonths,
        LoanApplicationStatus status,
        LocalDateTime submittedAt
        ) {
    public static LoanApplicationResponseDto from(LoanApplication loanApplication) {
        return new LoanApplicationResponseDto(
                loanApplication.getUser().getName(),
                loanApplication.getLoanProduct().getProductName(),
                loanApplication.getDisbursementAccount().getAccountNumber(),
                loanApplication.getRequestedAmount(),
                loanApplication.getRequestedTermMonths(),
                loanApplication.getStatus(),
                loanApplication.getSubmittedAt()
        );
    }
}
