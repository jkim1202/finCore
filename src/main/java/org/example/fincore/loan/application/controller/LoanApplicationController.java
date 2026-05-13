package org.example.fincore.loan.application.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.example.fincore.loan.application.dto.LoanApplicationRequestDto;
import org.example.fincore.loan.application.dto.LoanApplicationResponseDto;
import org.example.fincore.loan.application.dto.LoanApplicationSearchResponseDto;
import org.example.fincore.loan.application.usecase.LoanApplicationSearchUseCase;
import org.example.fincore.loan.application.usecase.LoanApplyUseCase;
import org.example.fincore.security.FinCoreUserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/loan-applications")
@AllArgsConstructor
public class LoanApplicationController {
    private final LoanApplyUseCase loanApplyUseCase;
    private final LoanApplicationSearchUseCase loanApplicationSearchUseCase;
    @PostMapping("")
    public ResponseEntity<LoanApplicationResponseDto> createLoanApplication(
            @Valid @RequestBody LoanApplicationRequestDto loanApplicationRequestDto,
            @AuthenticationPrincipal FinCoreUserDetails userDetails
    ){
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(loanApplyUseCase.applyLoan(loanApplicationRequestDto, userDetails));
    }

    @GetMapping("/{applicationId}")
    public ResponseEntity<LoanApplicationSearchResponseDto> getLoanApplication(
            @PathVariable Long applicationId,
            @AuthenticationPrincipal FinCoreUserDetails userDetails
    ){
        return ResponseEntity.ok(
                loanApplicationSearchUseCase.searchLoanApplication(applicationId, userDetails)
        );
    }
}
