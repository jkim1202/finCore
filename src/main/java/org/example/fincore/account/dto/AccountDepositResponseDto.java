package org.example.fincore.account.dto;

import jakarta.validation.constraints.*;
import org.example.fincore.account.entity.Account;
import org.example.fincore.account.entity.AccountTransaction;
import org.example.fincore.account.entity.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AccountDepositResponseDto(
        Long accountId,
        String accountNumber,
        Long transactionId,
        TransactionType transactionType,
        BigDecimal amount,
        BigDecimal balanceAfter,
        LocalDateTime transactedAt
) {
        public static AccountDepositResponseDto from(AccountTransaction transaction) {
                Account account = transaction.getAccount();

                return new AccountDepositResponseDto(
                        account.getId(),
                        account.getAccountNumber(),
                        transaction.getId(),
                        transaction.getTransactionType(),
                        transaction.getAmount(),
                        transaction.getBalanceAfter(),
                        transaction.getTransactedAt()
                );
        }
}