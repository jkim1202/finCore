package org.example.fincore.loan.product.service;

import lombok.AllArgsConstructor;
import org.example.fincore.loan.product.dto.LoanProductsSearchRequestDto;
import org.example.fincore.loan.product.dto.LoanProductsSearchResponseDto;
import org.example.fincore.loan.product.repository.LoanProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class LoanProductService {
    private final LoanProductRepository loanProductRepository;

    @Transactional(readOnly = true)
    public Page<LoanProductsSearchResponseDto> getLoanProducts(LoanProductsSearchRequestDto requestDto) {

        Pageable pageable = PageRequest.of(
                requestDto.pageOrDefault(),
                requestDto.sizeOrDefault(),
                toSort(requestDto.sortOrDefault())
        );

        return loanProductRepository.findLoanProductByActive(pageable, true)
                .map(LoanProductsSearchResponseDto::from);
    }

    private Sort toSort(String sortBy) {
        return switch (sortBy) {
            case "MAX AMOUNT ASC" -> Sort.by(Sort.Direction.ASC, "maxAmount");
            default -> Sort.by(Sort.Direction.ASC, "baseInterestRate");
        };
    }
}
