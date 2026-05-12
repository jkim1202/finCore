package org.example.fincore.account.usecase;

import org.example.fincore.account.dto.AccountWithdrawRequestDto;
import org.example.fincore.account.dto.AccountWithdrawResponseDto;
import org.example.fincore.account.entity.Account;
import org.example.fincore.account.entity.AccountStatus;
import org.example.fincore.account.entity.AccountTransaction;
import org.example.fincore.account.entity.TransactionType;
import org.example.fincore.account.service.AccountService;
import org.example.fincore.common.exception.BusinessException;
import org.example.fincore.common.exception.ErrorCode;
import org.example.fincore.security.FinCoreUserDetails;
import org.example.fincore.user.entity.User;
import org.example.fincore.user.entity.UserRole;
import org.example.fincore.user.entity.UserStatus;
import org.example.fincore.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountWithdrawUseCaseTest {

    private AccountWithdrawUseCase accountWithdrawUseCase;

    @Mock
    private AccountService accountService;

    @Mock
    private UserService userService;

    @BeforeEach
    void setUp() {
        accountWithdrawUseCase = new AccountWithdrawUseCase(accountService, userService);
    }

    @DisplayName("출금 유스케이스는 락 계좌를 조회한 뒤 출금 결과를 반환한다")
    @Test
    void withdrawUsesLockedAccountAndReturnsResponse() {
        FinCoreUserDetails userDetails = userDetails(1L);
        User user = user(1L);
        Account account = account(10L, user, BigDecimal.valueOf(1_000));
        AccountWithdrawRequestDto request = new AccountWithdrawRequestDto(BigDecimal.valueOf(400));
        AccountTransaction transaction = transaction(100L, account, TransactionType.WITHDRAWAL, request.amount(), BigDecimal.valueOf(600));
        when(userService.findUserByUserDetails(userDetails)).thenReturn(user);
        when(accountService.findAccountForUpdate(user, account.getId())).thenReturn(account);
        when(accountService.withdraw(request.amount(), account)).thenReturn(transaction);

        AccountWithdrawResponseDto response = accountWithdrawUseCase.withdraw(account.getId(), userDetails, request);

        assertThat(response.transactionId()).isEqualTo(transaction.getId());
        assertThat(response.transactionType()).isEqualTo(TransactionType.WITHDRAWAL);
        assertThat(response.balanceAfter()).isEqualByComparingTo("600");
    }

    @DisplayName("출금 시 잔액 부족 예외는 그대로 전파한다")
    @Test
    void withdrawPropagatesInsufficientBalance() {
        FinCoreUserDetails userDetails = userDetails(1L);
        User user = user(1L);
        Account account = account(10L, user, BigDecimal.valueOf(100));
        AccountWithdrawRequestDto request = new AccountWithdrawRequestDto(BigDecimal.valueOf(101));
        when(userService.findUserByUserDetails(userDetails)).thenReturn(user);
        when(accountService.findAccountForUpdate(user, account.getId())).thenReturn(account);
        when(accountService.withdraw(request.amount(), account))
                .thenThrow(new BusinessException(ErrorCode.ACCOUNT_BALANCE_NOT_ENOUGH));

        assertThatThrownBy(() -> accountWithdrawUseCase.withdraw(account.getId(), userDetails, request))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ACCOUNT_BALANCE_NOT_ENOUGH));
    }

    private FinCoreUserDetails userDetails(Long id) {
        return new FinCoreUserDetails(id, "user@example.com", "encoded-password", UserStatus.ACTIVE,
                List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER")));
    }

    private User user(Long id) {
        return User.builder()
                .id(id)
                .email("user@example.com")
                .passwordHash("encoded-password")
                .name("Kim User")
                .phone("010-1234-5678")
                .birthDate(LocalDate.of(2000, 1, 1))
                .status(UserStatus.ACTIVE)
                .roles(Set.of(UserRole.CUSTOMER))
                .build();
    }

    private Account account(Long id, User user, BigDecimal balance) {
        return Account.builder()
                .id(id)
                .user(user)
                .accountNumber("110-000-000001")
                .accountStatus(AccountStatus.ACTIVE)
                .balance(balance)
                .build();
    }

    private AccountTransaction transaction(Long id, Account account, TransactionType type, BigDecimal amount, BigDecimal balanceAfter) {
        return AccountTransaction.builder()
                .id(id)
                .account(account)
                .transactionType(type)
                .amount(amount)
                .balanceAfter(balanceAfter)
                .build();
    }
}
