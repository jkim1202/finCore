package org.example.fincore.account.usecase;

import lombok.AllArgsConstructor;
import org.example.fincore.account.dto.AccountDepositRequestDto;
import org.example.fincore.account.dto.AccountDepositResponseDto;
import org.example.fincore.account.entity.Account;
import org.example.fincore.account.repository.AccountTransactionRepository;
import org.example.fincore.account.service.AccountService;
import org.example.fincore.security.FinCoreUserDetails;
import org.example.fincore.user.entity.User;
import org.example.fincore.user.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class AccountDepositUseCase {
    private AccountService accountService;
    private UserService userService;
    private AccountTransactionRepository accountTransactionRepository;

    @Transactional
    public AccountDepositResponseDto deposit(Long accountId, FinCoreUserDetails userDetails, AccountDepositRequestDto accountDepositRequestDto) {
        User user = userService.findUserByUserDetails(userDetails);

        Account account = accountService.findAccount(user, accountId);

        return accountService.deposit(accountDepositRequestDto.amount(), account);
    }
}
