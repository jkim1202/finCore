package org.example.fincore.loan.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record LoanApplicationRequestDto(
        @NotNull Long loanProductId,
        @NotBlank String disbursementAccount,
        @NotNull @Positive BigDecimal requestedAmount,
        @NotNull @Positive Integer requestedTermMonths,
        @NotNull @PositiveOrZero BigDecimal annualIncome,
        @PositiveOrZero BigDecimal existingDebtAmount
        ) {
}
