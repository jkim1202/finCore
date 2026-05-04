package org.example.fincore.account.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.example.fincore.account.dto.TransactionViewRequestDto;
import org.example.fincore.account.dto.TransactionViewResponseDto;
import org.example.fincore.account.usecase.AccountQueryUseCase;
import org.example.fincore.security.FinCoreUserDetails;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/accounts")
@AllArgsConstructor
public class AccountTransactionController {
    private final AccountQueryUseCase accountQueryUseCase;

    @GetMapping("/{accountId}/transactions")
    public ResponseEntity<Page<TransactionViewResponseDto>> getTransactions(
            @PathVariable Long accountId,
            @AuthenticationPrincipal FinCoreUserDetails userDetails,
            @Valid @ModelAttribute TransactionViewRequestDto request
    ) {
        Page<TransactionViewResponseDto> response =
                accountQueryUseCase.getTransactions(accountId, userDetails, request);

        return  ResponseEntity.ok(response);
    }

    @GetMapping("/transactions/{transactionId}")
    public ResponseEntity<TransactionViewResponseDto> getTransactions(
            @PathVariable Long transactionId,
            @AuthenticationPrincipal FinCoreUserDetails userDetails
    ) {
        TransactionViewResponseDto response =
                accountQueryUseCase.getTransaction(transactionId, userDetails);

        return  ResponseEntity.ok(response);
    }
}
