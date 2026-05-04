package org.example.fincore.account.dto;

import org.example.fincore.account.entity.Account;
import org.example.fincore.account.entity.AccountStatus;

import java.math.BigDecimal;

public record AccountDetailResponseDto(
        Long accountId,
        String accountNumber,
        AccountStatus accountStatus,
        BigDecimal balance
) {
    public static AccountDetailResponseDto from(Account account) {
        return new AccountDetailResponseDto(
                account.getId(),
                account.getAccountNumber(),
                account.getAccountStatus(),
                account.getBalance()
        );
    }
}
