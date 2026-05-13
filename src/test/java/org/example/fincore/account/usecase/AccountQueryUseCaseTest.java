package org.example.fincore.account.usecase;

import org.example.fincore.account.dto.AccountDetailResponseDto;
import org.example.fincore.account.dto.TransactionViewRequestDto;
import org.example.fincore.account.dto.TransactionViewResponseDto;
import org.example.fincore.account.entity.Account;
import org.example.fincore.account.entity.AccountStatus;
import org.example.fincore.account.entity.AccountTransaction;
import org.example.fincore.account.entity.TransactionType;
import org.example.fincore.account.repository.AccountTransactionRepository;
import org.example.fincore.account.service.AccountService;
import org.example.fincore.common.exception.BusinessException;
import org.example.fincore.common.exception.ErrorCode;
import org.example.fincore.security.FinCoreUserDetails;
import org.example.fincore.user.entity.User;
import org.example.fincore.user.entity.UserRole;
import org.example.fincore.user.entity.UserStatus;
import org.example.fincore.user.component.UserReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountQueryUseCaseTest {

    private AccountQueryUseCase accountQueryUseCase;

    @Mock
    private AccountService accountService;

    @Mock
    private UserReader userReader;

    @Mock
    private AccountTransactionRepository accountTransactionRepository;

    @BeforeEach
    void setUp() {
        accountQueryUseCase = new AccountQueryUseCase(accountService, userReader, accountTransactionRepository);
    }

    @DisplayName("계좌 상세 조회는 소유자 검증 후 계좌 정보를 반환한다")
    @Test
    void getAccountDetailReturnsOwnedAccount() {
        FinCoreUserDetails userDetails = userDetails(1L);
        User user = user(1L);
        Account account = account(10L, user, BigDecimal.valueOf(1_000));
        when(userReader.getActiveUser(userDetails)).thenReturn(user);
        when(accountService.findAccount(user, account.getId())).thenReturn(account);

        AccountDetailResponseDto response = accountQueryUseCase.getAccountDetail(account.getId(), userDetails);

        assertThat(response.accountId()).isEqualTo(account.getId());
        assertThat(response.balance()).isEqualByComparingTo("1000");
    }

    @DisplayName("거래내역 목록 조회는 소유자 검증 후 페이징된 거래 목록을 반환한다")
    @Test
    void getTransactionsReturnsPagedTransactions() {
        FinCoreUserDetails userDetails = userDetails(1L);
        User user = user(1L);
        Account account = account(10L, user, BigDecimal.valueOf(1_000));
        AccountTransaction transaction = transaction(100L, account, TransactionType.DEPOSIT, BigDecimal.valueOf(500), BigDecimal.valueOf(1_500));
        when(userReader.getActiveUser(userDetails)).thenReturn(user);
        when(accountService.findAccount(user, account.getId())).thenReturn(account);
        when(accountTransactionRepository.findByAccountId(eq(account.getId()), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(transaction)));

        Page<TransactionViewResponseDto> response =
                accountQueryUseCase.getTransactions(account.getId(), userDetails, new TransactionViewRequestDto(0, 20));

        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).transactionId()).isEqualTo(transaction.getId());
    }

    @DisplayName("거래 단건 조회는 소유자 거래이면 상세 정보를 반환한다")
    @Test
    void getTransactionReturnsOwnedTransaction() {
        FinCoreUserDetails userDetails = userDetails(1L);
        User user = user(1L);
        Account account = account(10L, user, BigDecimal.valueOf(1_000));
        AccountTransaction transaction = transaction(100L, account, TransactionType.DEPOSIT, BigDecimal.valueOf(500), BigDecimal.valueOf(1_500));
        when(userReader.getActiveUser(userDetails)).thenReturn(user);
        when(accountTransactionRepository.findByTransactionId(transaction.getId())).thenReturn(Optional.of(transaction));

        TransactionViewResponseDto response = accountQueryUseCase.getTransaction(transaction.getId(), userDetails);

        assertThat(response.transactionId()).isEqualTo(transaction.getId());
    }

    @DisplayName("거래 단건 조회 시 거래가 없으면 ACCOUNT_NOT_FOUND 예외를 던진다")
    @Test
    void getTransactionRejectsMissingTransaction() {
        FinCoreUserDetails userDetails = userDetails(1L);
        User user = user(1L);
        when(userReader.getActiveUser(userDetails)).thenReturn(user);
        when(accountTransactionRepository.findByTransactionId(100L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountQueryUseCase.getTransaction(100L, userDetails))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ACCOUNT_NOT_FOUND));
    }

    @DisplayName("거래 단건 조회 시 다른 사용자의 거래이면 권한 예외를 던진다")
    @Test
    void getTransactionRejectsTransactionOwnedByAnotherUser() {
        FinCoreUserDetails userDetails = userDetails(2L);
        User requester = user(2L);
        User owner = user(1L);
        Account account = account(10L, owner, BigDecimal.valueOf(1_000));
        AccountTransaction transaction = transaction(100L, account, TransactionType.DEPOSIT, BigDecimal.valueOf(500), BigDecimal.valueOf(1_500));
        when(userReader.getActiveUser(userDetails)).thenReturn(requester);
        when(accountTransactionRepository.findByTransactionId(transaction.getId())).thenReturn(Optional.of(transaction));

        assertThatThrownBy(() -> accountQueryUseCase.getTransaction(transaction.getId(), userDetails))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ACCOUNT_NOT_BELONG_TO_USER));
    }

    private FinCoreUserDetails userDetails(Long id) {
        return new FinCoreUserDetails(id, "user" + id + "@example.com", "encoded-password", UserStatus.ACTIVE,
                List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER")));
    }

    private User user(Long id) {
        return User.builder()
                .id(id)
                .email("user" + id + "@example.com")
                .passwordHash("encoded-password")
                .name("Kim User")
                .phone("010-1234-000" + id)
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
