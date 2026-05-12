package org.example.fincore.loan.product.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record LoanProductsSearchRequestDto(
        @Min(0) Integer page,
        @Min(1) @Max(10) Integer size,
        String sort
) {
    public int pageOrDefault() {
        return page == null ? 0 : page;
    }

    public int sizeOrDefault() {
        return size == null ? 10 : size;
    }

    public String sortOrDefault() {
        return sort == null ? "DEFAULT" : sort.toUpperCase();
    }
}
