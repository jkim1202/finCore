package org.example.fincore.loan.product.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.example.fincore.loan.product.dto.LoanProductsSearchRequestDto;
import org.example.fincore.loan.product.dto.LoanProductsSearchResponseDto;
import org.example.fincore.loan.product.service.LoanProductService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/loan-products")
@AllArgsConstructor
public class LoanProductController {
    private final LoanProductService loanService;

    @GetMapping("")
    public ResponseEntity<Page<LoanProductsSearchResponseDto>> getLoanProducts(
            @Valid @ModelAttribute LoanProductsSearchRequestDto loanProductsSearchRequestDto
    ) {
        return ResponseEntity.ok(loanService.getLoanProducts(loanProductsSearchRequestDto));
    }
}
