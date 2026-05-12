package org.example.fincore.account.usecase;

import org.example.fincore.account.dto.AccountCreateResponseDto;
import org.example.fincore.account.entity.Account;
import org.example.fincore.account.entity.AccountStatus;
import org.example.fincore.account.service.AccountNumberGenerator;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountCreateUseCaseTest {

    private AccountCreateUseCase accountCreateUseCase;

    @Mock
    private AccountService accountService;

    @Mock
    private UserService userService;

    @Mock
    private AccountNumberGenerator accountNumberGenerator;

    @BeforeEach
    void setUp() {
        accountCreateUseCase = new AccountCreateUseCase(accountService, userService, accountNumberGenerator);
    }

    @DisplayName("인증 사용자로 계좌번호를 생성하고 계좌를 개설한다")
    @Test
    void createAccountCreatesAccountForAuthenticatedUser() {
        FinCoreUserDetails userDetails = userDetails(1L);
        User user = user(1L);
        Account account = account(10L, user, BigDecimal.ZERO);
        when(userService.findUserByUserDetails(userDetails)).thenReturn(user);
        when(accountNumberGenerator.generate()).thenReturn("110-000-000001");
        when(accountService.createAccount("110-000-000001", user)).thenReturn(account);

        AccountCreateResponseDto response = accountCreateUseCase.createAccount(userDetails);

        assertThat(response.userId()).isEqualTo(user.getId());
        assertThat(response.accountId()).isEqualTo(account.getId());
        assertThat(response.accountNumber()).isEqualTo(account.getAccountNumber());
        assertThat(response.status()).isEqualTo(AccountStatus.ACTIVE);
        verify(accountNumberGenerator).generate();
    }

    @DisplayName("계좌 생성 시 사용자 조회 실패는 그대로 전파한다")
    @Test
    void createAccountPropagatesUserLookupFailure() {
        FinCoreUserDetails userDetails = userDetails(1L);
        when(userService.findUserByUserDetails(userDetails))
                .thenThrow(new BusinessException(ErrorCode.USER_NOT_FOUND));

        assertThatThrownBy(() -> accountCreateUseCase.createAccount(userDetails))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND));
    }

    private FinCoreUserDetails userDetails(Long id) {
        return new FinCoreUserDetails(
                id,
                "user@example.com",
                "encoded-password",
                UserStatus.ACTIVE,
                List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER"))
        );
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
