package org.example.fincore.account.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.example.fincore.account.dto.*;
import org.example.fincore.account.usecase.AccountCreateUseCase;
import org.example.fincore.account.usecase.AccountDepositUseCase;
import org.example.fincore.account.usecase.AccountQueryUseCase;
import org.example.fincore.account.usecase.AccountWithdrawUseCase;
import org.example.fincore.security.FinCoreUserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/accounts")
@AllArgsConstructor
public class AccountController {
    private final AccountCreateUseCase accountCreateUseCase;
    private final AccountQueryUseCase accountQueryUseCase;
    private final AccountDepositUseCase accountDepositUseCase;
    private final AccountWithdrawUseCase accountWithdrawUseCase;

    @PostMapping
    public ResponseEntity<AccountCreateResponseDto> createAccount(@AuthenticationPrincipal FinCoreUserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(accountCreateUseCase.createAccount(userDetails));
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<AccountDetailResponseDto> getAccount(
            @PathVariable Long accountId,
            @AuthenticationPrincipal FinCoreUserDetails userDetails
    ) {
        return ResponseEntity.ok(accountQueryUseCase.getAccountDetail(accountId, userDetails));
    }

    @PostMapping("/{accountId}/deposit")
    public ResponseEntity<AccountDepositResponseDto> deposit(
            @PathVariable Long accountId,
            @AuthenticationPrincipal FinCoreUserDetails userDetails,
            @Valid @RequestBody AccountDepositRequestDto request
    ) {
        AccountDepositResponseDto response =
                accountDepositUseCase.deposit(accountId, userDetails, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{accountId}/withdraw")
    public ResponseEntity<AccountWithdrawResponseDto> withdraw(
            @PathVariable Long accountId,
            @AuthenticationPrincipal FinCoreUserDetails userDetails,
            @Valid @RequestBody AccountWithdrawRequestDto request
    ) {
        AccountWithdrawResponseDto response =
                accountWithdrawUseCase.withdraw(accountId, userDetails, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
