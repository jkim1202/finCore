package org.example.fincore.account.usecase;

import lombok.AllArgsConstructor;
import org.example.fincore.account.dto.AccountWithdrawRequestDto;
import org.example.fincore.account.dto.AccountWithdrawResponseDto;
import org.example.fincore.account.entity.Account;
import org.example.fincore.account.service.AccountService;
import org.example.fincore.security.FinCoreUserDetails;
import org.example.fincore.user.component.UserReader;
import org.example.fincore.user.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class AccountWithdrawUseCase {
    private final AccountService accountService;
    private final UserReader userReader;

    @Transactional
    public AccountWithdrawResponseDto withdraw(Long accountId, FinCoreUserDetails userDetails, AccountWithdrawRequestDto accountWithdrawRequestDto) {
        User user = userReader.getActiveUser(userDetails);

        // 계좌 (비관적) 락 획득 시도
        Account account = accountService.findAccountForUpdate(user, accountId);

        return AccountWithdrawResponseDto.from(accountService.withdraw(accountWithdrawRequestDto.amount(), account));
    }
}
