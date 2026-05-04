package org.example.fincore.account.dto;

import org.example.fincore.account.entity.Account;
import org.example.fincore.account.entity.AccountStatus;

import java.time.LocalDateTime;

public record AccountCreateResponseDto(
        Long userId,
        Long accountId,
        String accountNumber,
        AccountStatus status,
        LocalDateTime createAt
) {
    public static AccountCreateResponseDto from(Account account){
        return new AccountCreateResponseDto(
                account.getUser().getId(),
                account.getId(),
                account.getAccountNumber(),
                account.getAccountStatus(),
                account.getCreatedAt()
        );
    }
}
