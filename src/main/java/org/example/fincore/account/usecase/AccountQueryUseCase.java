package org.example.fincore.account.usecase;

import lombok.AllArgsConstructor;
import org.example.fincore.account.dto.AccountDetailResponseDto;
import org.example.fincore.account.dto.TransactionViewRequestDto;
import org.example.fincore.account.dto.TransactionViewResponseDto;
import org.example.fincore.account.entity.Account;
import org.example.fincore.account.entity.AccountTransaction;
import org.example.fincore.account.repository.AccountTransactionRepository;
import org.example.fincore.account.service.AccountService;
import org.example.fincore.common.exception.BusinessException;
import org.example.fincore.common.exception.ErrorCode;
import org.example.fincore.security.FinCoreUserDetails;
import org.example.fincore.user.component.UserReader;
import org.example.fincore.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class AccountQueryUseCase {
    private final AccountService accountService;
    private final UserReader userReader;
    private final AccountTransactionRepository accountTransactionRepository;

    @Transactional(readOnly = true)
    public AccountDetailResponseDto getAccountDetail(Long accountId, FinCoreUserDetails userDetails) {
        User user = userReader.getActiveUser(userDetails);

        Account account = accountService.findAccount(user, accountId);

        return AccountDetailResponseDto.from(account);
    }

    @Transactional(readOnly = true)
    public Page<TransactionViewResponseDto> getTransactions(Long accountId, FinCoreUserDetails userDetails, TransactionViewRequestDto requestDto) {
        User user = userReader.getActiveUser(userDetails);

        Account account = accountService.findAccount(user, accountId);

        Pageable pageable = PageRequest.of(requestDto.page(), requestDto.size(), Sort.by(Sort.Direction.DESC, "transactedAt"));
        return accountTransactionRepository
                .findByAccountId(account.getId(), pageable)
                .map(TransactionViewResponseDto::from);
    }

    @Transactional(readOnly = true)
    public TransactionViewResponseDto getTransaction(Long transactionId, FinCoreUserDetails userDetails) {
        User user = userReader.getActiveUser(userDetails);

        AccountTransaction transaction = accountTransactionRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ACCOUNT_NOT_FOUND));

        if(!transaction.getAccount().getUser().getId().equals(user.getId())) {
            throw new BusinessException(ErrorCode.ACCOUNT_NOT_BELONG_TO_USER);
        }
        return TransactionViewResponseDto.from(transaction);
    }
}
