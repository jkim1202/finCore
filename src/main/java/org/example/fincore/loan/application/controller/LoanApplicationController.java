package org.example.fincore.loan.application.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.example.fincore.loan.application.dto.LoanApplicationRequestDto;
import org.example.fincore.loan.application.dto.LoanApplicationResponseDto;
import org.example.fincore.loan.application.usecase.LoanApplyUseCase;
import org.example.fincore.security.FinCoreUserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/loan-applications")
@AllArgsConstructor
public class LoanApplicationController {
    private final LoanApplyUseCase loanApplyUseCase;
    @PostMapping("")
    public ResponseEntity<LoanApplicationResponseDto> createLoanApplication(
            @Valid @RequestBody LoanApplicationRequestDto loanApplicationRequestDto,
            @AuthenticationPrincipal FinCoreUserDetails userDetails
    ){
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(loanApplyUseCase.applyLoan(loanApplicationRequestDto, userDetails));
    }
}
