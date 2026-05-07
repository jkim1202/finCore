package org.example.fincore.loan.product.dto;

import org.example.fincore.loan.product.entity.LoanProduct;

import java.math.BigDecimal;

public record LoanProductsSearchResponseDto(
        Long loanProductId,
        String productName,
        BigDecimal minAmount,
        BigDecimal maxAmount,
        BigDecimal baseInterestRate,
        BigDecimal overdueInterestRate,
        Integer minTermMonths,
        Integer maxTermMonths,
        Boolean active
) {
    public static LoanProductsSearchResponseDto from(LoanProduct loanProduct) {
        return new LoanProductsSearchResponseDto(
                loanProduct.getLoanProductId(),
                loanProduct.getProductName(),
                loanProduct.getMinAmount(),
                loanProduct.getMaxAmount(),
                loanProduct.getBaseInterestRate(),
                loanProduct.getOverdueInterestRate(),
                loanProduct.getMinTermMonths(),
                loanProduct.getMaxTermMonths(),
                loanProduct.getActive()
        );
    }
}
