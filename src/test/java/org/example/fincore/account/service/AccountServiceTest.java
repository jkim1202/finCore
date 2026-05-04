package org.example.fincore.account.service;

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
import org.example.fincore.user.entity.UserRole;
import org.example.fincore.user.entity.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    private AccountService accountService;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountTransactionRepository accountTransactionRepository;

    @BeforeEach
    void setUp() {
        accountService = new AccountService(accountRepository, accountTransactionRepository);
    }

    @DisplayName("계좌 생성 시 활성 상태와 0원 잔액으로 저장한다")
    @Test
    void createAccountSavesActiveAccountWithZeroBalance() {
        User user = user(1L, UserStatus.ACTIVE);
        Account savedAccount = account(10L, user, AccountStatus.ACTIVE, BigDecimal.ZERO);
        when(accountRepository.save(any(Account.class))).thenReturn(savedAccount);

        Account result = accountService.createAccount("110-000-000001", user);

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(captor.capture());
        Account account = captor.getValue();
        assertThat(account.getUser()).isEqualTo(user);
        assertThat(account.getAccountNumber()).isEqualTo("110-000-000001");
        assertThat(account.getAccountStatus()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(account.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result).isEqualTo(savedAccount);
    }

    @DisplayName("소유자의 활성 계좌 조회가 성공한다")
    @Test
    void findAccountReturnsOwnedActiveAccount() {
        User user = user(1L, UserStatus.ACTIVE);
        Account account = account(10L, user, AccountStatus.ACTIVE, BigDecimal.ZERO);
        when(accountRepository.findById(account.getId())).thenReturn(Optional.of(account));

        Account result = accountService.findAccount(user, account.getId());

        assertThat(result).isEqualTo(account);
    }

    @DisplayName("존재하지 않는 계좌 조회 시 ACCOUNT_NOT_FOUND 예외를 던진다")
    @Test
    void findAccountRejectsMissingAccount() {
        User user = user(1L, UserStatus.ACTIVE);
        when(accountRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.findAccount(user, 10L))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ACCOUNT_NOT_FOUND));
    }

    @DisplayName("다른 사용자의 계좌 조회 시 ACCOUNT_NOT_BELONG_TO_USER 예외를 던진다")
    @Test
    void findAccountRejectsAccountOwnedByAnotherUser() {
        User owner = user(1L, UserStatus.ACTIVE);
        User requester = user(2L, UserStatus.ACTIVE);
        Account account = account(10L, owner, AccountStatus.ACTIVE, BigDecimal.ZERO);
        when(accountRepository.findById(account.getId())).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> accountService.findAccount(requester, account.getId()))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ACCOUNT_NOT_BELONG_TO_USER));
    }

    @DisplayName("비활성 계좌 조회 시 ACCOUNT_STATUS_NOT_ACTIVE 예외를 던진다")
    @Test
    void findAccountRejectsInactiveAccount() {
        User user = user(1L, UserStatus.ACTIVE);
        Account account = account(10L, user, AccountStatus.FROZEN, BigDecimal.ZERO);
        when(accountRepository.findById(account.getId())).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> accountService.findAccount(user, account.getId()))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ACCOUNT_STATUS_NOT_ACTIVE));
    }

    @DisplayName("수정용 계좌 조회는 비관적 락 Repository 경로를 사용한다")
    @Test
    void findAccountForUpdateUsesLockingRepositoryPath() {
        User user = user(1L, UserStatus.ACTIVE);
        Account account = account(10L, user, AccountStatus.ACTIVE, BigDecimal.ZERO);
        when(accountRepository.findByIdForUpdate(account.getId())).thenReturn(Optional.of(account));

        Account result = accountService.findAccountForUpdate(user, account.getId());

        assertThat(result).isEqualTo(account);
        verify(accountRepository).findByIdForUpdate(account.getId());
    }

    @DisplayName("입금 시 잔액을 증가시키고 입금 거래내역을 저장한다")
    @Test
    void depositIncreasesBalanceAndStoresTransaction() {
        User user = user(1L, UserStatus.ACTIVE);
        Account account = account(10L, user, AccountStatus.ACTIVE, BigDecimal.valueOf(1_000));
        when(accountRepository.save(account)).thenReturn(account);
        when(accountTransactionRepository.save(any(AccountTransaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AccountDepositResponseDto response = accountService.deposit(BigDecimal.valueOf(500), account);

        assertThat(account.getBalance()).isEqualByComparingTo("1500");
        assertThat(response.transactionType()).isEqualTo(TransactionType.DEPOSIT);
        assertThat(response.amount()).isEqualByComparingTo("500");
        assertThat(response.balanceAfter()).isEqualByComparingTo("1500");
    }

    @DisplayName("출금 시 잔액을 차감하고 출금 거래내역을 저장한다")
    @Test
    void withdrawDecreasesBalanceAndStoresTransaction() {
        User user = user(1L, UserStatus.ACTIVE);
        Account account = account(10L, user, AccountStatus.ACTIVE, BigDecimal.valueOf(1_000));
        when(accountRepository.save(account)).thenReturn(account);
        when(accountTransactionRepository.save(any(AccountTransaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AccountTransaction transaction = accountService.withdraw(BigDecimal.valueOf(400), account);

        assertThat(account.getBalance()).isEqualByComparingTo("600");
        assertThat(transaction.getTransactionType()).isEqualTo(TransactionType.WITHDRAWAL);
        assertThat(transaction.getAmount()).isEqualByComparingTo("400");
        assertThat(transaction.getBalanceAfter()).isEqualByComparingTo("600");
    }

    @DisplayName("잔액이 부족하면 출금을 거부한다")
    @Test
    void withdrawRejectsInsufficientBalance() {
        User user = user(1L, UserStatus.ACTIVE);
        Account account = account(10L, user, AccountStatus.ACTIVE, BigDecimal.valueOf(100));

        assertThatThrownBy(() -> accountService.withdraw(BigDecimal.valueOf(101), account))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ACCOUNT_BALANCE_NOT_ENOUGH));
    }

    private User user(Long id, UserStatus status) {
        return User.builder()
                .id(id)
                .email("user" + id + "@example.com")
                .passwordHash("encoded-password")
                .name("Kim User")
                .phone("010-1234-000" + id)
                .birthDate(LocalDate.of(2000, 1, 1))
                .status(status)
                .roles(Set.of(UserRole.CUSTOMER))
                .build();
    }

    private Account account(Long id, User user, AccountStatus status, BigDecimal balance) {
        return Account.builder()
                .id(id)
                .user(user)
                .accountNumber("110-000-000001")
                .accountStatus(status)
                .balance(balance)
                .build();
    }
}
