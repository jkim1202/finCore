package org.example.fincore.account.service;

import lombok.AllArgsConstructor;
import org.example.fincore.account.dto.AccountDepositResponseDto;
import org.example.fincore.account.entity.Account;
import org.example.fincore.account.entity.AccountStatus;
import org.example.fincore.account.entity.AccountTransaction;
import org.example.fincore.account.entity.TransactionType;
import org.example.fincore.account.repository.AccountRepository;
import org.example.fincore.account.repository.AccountTransactionRepository;
import org.example.fincore.exception.BusinessException;
import org.example.fincore.exception.ErrorCode;
import org.example.fincore.user.entity.User;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@AllArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private final AccountTransactionRepository accountTransactionRepository;

    public Account createAccount(String accountNumber, User user) {
        return accountRepository.save(Account.builder()
                .user(user)
                .accountNumber(accountNumber)
                .accountStatus(AccountStatus.ACTIVE)
                .balance(BigDecimal.ZERO)
                .build());
    }


    public Account findAccount(User user, Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ACCOUNT_NOT_FOUND));

        validateUserOwnsAccount(user, account);

        validateAccountStatusValid(account);

        return account;
    }
    public Account findAccountForUpdate(User user, Long accountId) {
        Account account = accountRepository.findByIdForUpdate(accountId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ACCOUNT_NOT_FOUND));

        validateUserOwnsAccount(user, account);

        validateAccountStatusValid(account);

        return account;
    }
    private void validateUserOwnsAccount(User user, Account account) {
        if(!account.getUser().getId().equals(user.getId())) {
            throw new BusinessException(ErrorCode.ACCOUNT_NOT_BELONG_TO_USER);
        }
    }
    private void validateAccountStatusValid(Account account) {
        if(!account.getAccountStatus().equals(AccountStatus.ACTIVE)) {
            throw new BusinessException(ErrorCode.ACCOUNT_STATUS_NOT_ACTIVE);
        }
    }

    public AccountDepositResponseDto deposit(BigDecimal amount, Account account) {
        account.deposit(amount);

        accountRepository.save(account);

        // Redis 추가시 accountTransactionRepository.existsByIdempotencyKey 로 중복 검사 추가 예정.

        AccountTransaction accountTransaction = AccountTransaction
                .builder()
                .account(account)
                .transactionType(TransactionType.DEPOSIT)
                .amount(amount)
                .balanceAfter(account.getBalance())
                .idempotencyKey(null) // Redis 추가시 값 바꿀 예정
                .build();

        return AccountDepositResponseDto.from(accountTransactionRepository.save(accountTransaction));
    }

    public AccountTransaction withdraw(BigDecimal amount, Account account) {
        account.withdraw(amount);

        accountRepository.save(account);

        // Redis 추가시 accountTransactionRepository.existsByIdempotencyKey 로 중복 검사 추가 예정.

        AccountTransaction accountTransaction = AccountTransaction
                .builder()
                .account(account)
                .transactionType(TransactionType.WITHDRAWAL)
                .amount(amount)
                .balanceAfter(account.getBalance())
                .idempotencyKey(null) // Redis 추가시 값 바꿀 예정
                .build();

        return accountTransactionRepository.save(accountTransaction);
    }
}
