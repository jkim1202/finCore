package org.example.fincore.account.usecase;

import lombok.AllArgsConstructor;
import org.example.fincore.account.dto.AccountCreateResponseDto;
import org.example.fincore.account.entity.Account;
import org.example.fincore.account.service.AccountNumberGenerator;
import org.example.fincore.account.service.AccountService;
import org.example.fincore.security.FinCoreUserDetails;
import org.example.fincore.user.component.UserReader;
import org.example.fincore.user.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class AccountCreateUseCase {
    private final AccountService accountService;
    private final UserReader userReader;
    private final AccountNumberGenerator accountNumberGenerator;

    @Transactional
    public AccountCreateResponseDto createAccount(FinCoreUserDetails userDetails){
        User user = userReader.getActiveUser(userDetails);

        String accountNumber = accountNumberGenerator.generate();

        Account createdAccount = accountService.createAccount(accountNumber, user);

        return AccountCreateResponseDto.from(createdAccount);
    }
}
