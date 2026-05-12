package org.example.fincore.account.usecase;

import org.example.fincore.account.dto.AccountDepositRequestDto;
import org.example.fincore.account.dto.AccountDepositResponseDto;
import org.example.fincore.account.entity.Account;
import org.example.fincore.account.entity.AccountStatus;
import org.example.fincore.account.entity.TransactionType;
import org.example.fincore.account.repository.AccountTransactionRepository;
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
class AccountDepositUseCaseTest {

    private AccountDepositUseCase accountDepositUseCase;

    @Mock
    private AccountService accountService;

    @Mock
    private UserService userService;

    @Mock
    private AccountTransactionRepository accountTransactionRepository;

    @BeforeEach
    void setUp() {
        accountDepositUseCase = new AccountDepositUseCase(accountService, userService, accountTransactionRepository);
    }

    @DisplayName("입금 유스케이스는 사용자와 계좌를 검증한 뒤 입금 처리를 위임한다")
    @Test
    void depositDelegatesToAccountService() {
        FinCoreUserDetails userDetails = userDetails(1L);
        User user = user(1L);
        Account account = account(10L, user, BigDecimal.valueOf(1_000));
        AccountDepositRequestDto request = new AccountDepositRequestDto(BigDecimal.valueOf(500));
        AccountDepositResponseDto expected = new AccountDepositResponseDto(
                account.getId(),
                account.getAccountNumber(),
                100L,
                TransactionType.DEPOSIT,
                request.amount(),
                BigDecimal.valueOf(1_500),
                null
        );
        when(userService.findUserByUserDetails(userDetails)).thenReturn(user);
        when(accountService.findAccount(user, account.getId())).thenReturn(account);
        when(accountService.deposit(request.amount(), account)).thenReturn(expected);

        AccountDepositResponseDto response = accountDepositUseCase.deposit(account.getId(), userDetails, request);

        assertThat(response).isEqualTo(expected);
    }

    @DisplayName("입금 시 계좌 조회 실패는 그대로 전파한다")
    @Test
    void depositPropagatesAccountFailure() {
        FinCoreUserDetails userDetails = userDetails(1L);
        User user = user(1L);
        AccountDepositRequestDto request = new AccountDepositRequestDto(BigDecimal.valueOf(500));
        when(userService.findUserByUserDetails(userDetails)).thenReturn(user);
        when(accountService.findAccount(user, 10L))
                .thenThrow(new BusinessException(ErrorCode.ACCOUNT_NOT_FOUND));

        assertThatThrownBy(() -> accountDepositUseCase.deposit(10L, userDetails, request))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ACCOUNT_NOT_FOUND));
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
}
