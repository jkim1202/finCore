package org.example.fincore.loan.application.usecase;

import org.example.fincore.account.entity.Account;
import org.example.fincore.account.entity.AccountStatus;
import org.example.fincore.account.service.AccountService;
import org.example.fincore.common.exception.BusinessException;
import org.example.fincore.common.exception.ErrorCode;
import org.example.fincore.loan.application.dto.LoanApplicationRequestDto;
import org.example.fincore.loan.application.dto.LoanApplicationResponseDto;
import org.example.fincore.loan.application.entity.LoanApplication;
import org.example.fincore.loan.application.entity.LoanApplicationStatus;
import org.example.fincore.loan.application.repository.LoanApplicationRepository;
import org.example.fincore.loan.product.entity.LoanProduct;
import org.example.fincore.loan.product.service.LoanProductService;
import org.example.fincore.security.FinCoreUserDetails;
import org.example.fincore.user.entity.User;
import org.example.fincore.user.entity.UserRole;
import org.example.fincore.user.entity.UserStatus;
import org.example.fincore.user.component.UserReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoanApplyUseCaseTest {

    private LoanApplyUseCase loanApplyUseCase;

    @Mock
    private AccountService accountService;

    @Mock
    private UserReader userReader;

    @Mock
    private LoanProductService loanProductService;

    @Mock
    private LoanApplicationRepository loanApplicationRepository;

    @BeforeEach
    void setUp() {
        loanApplyUseCase = new LoanApplyUseCase(
                accountService,
                userReader,
                loanProductService,
                loanApplicationRepository
        );
    }

    @DisplayName("대출 신청 유스케이스는 사용자, 상품, 입금 계좌를 검증한 뒤 신청을 저장한다")
    @Test
    void applyLoanCreatesAndSavesLoanApplication() {
        FinCoreUserDetails userDetails = userDetails(1L);
        User user = user(1L);
        LoanProduct loanProduct = loanProduct();
        Account account = account(user);
        LoanApplicationRequestDto request = request();
        when(userReader.getActiveUser(userDetails)).thenReturn(user);
        when(loanProductService.getLoanProduct(loanProduct.getLoanProductId())).thenReturn(loanProduct);
        when(accountService.findAccountByAccountNumberAndUser(account.getAccountNumber(), user)).thenReturn(account);
        when(loanApplicationRepository.save(org.mockito.ArgumentMatchers.any(LoanApplication.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        LoanApplicationResponseDto response = loanApplyUseCase.applyLoan(request, userDetails);

        ArgumentCaptor<LoanApplication> captor = ArgumentCaptor.forClass(LoanApplication.class);
        verify(loanApplicationRepository).save(captor.capture());
        LoanApplication savedApplication = captor.getValue();
        assertThat(savedApplication.getUser()).isEqualTo(user);
        assertThat(savedApplication.getLoanProduct()).isEqualTo(loanProduct);
        assertThat(savedApplication.getDisbursementAccount()).isEqualTo(account);
        assertThat(savedApplication.getRequestedAmount()).isEqualByComparingTo(request.requestedAmount());
        assertThat(savedApplication.getRequestedTermMonths()).isEqualTo(request.requestedTermMonths());
        assertThat(savedApplication.getStatus()).isEqualTo(LoanApplicationStatus.SUBMITTED);

        assertThat(response.userName()).isEqualTo(user.getName());
        assertThat(response.loanProductName()).isEqualTo(loanProduct.getProductName());
        assertThat(response.disbursementAccountNumber()).isEqualTo(account.getAccountNumber());
        assertThat(response.status()).isEqualTo(LoanApplicationStatus.SUBMITTED);
    }

    @DisplayName("대출 상품 조회 실패는 신청 저장 없이 그대로 전파한다")
    @Test
    void applyLoanPropagatesLoanProductFailure() {
        FinCoreUserDetails userDetails = userDetails(1L);
        User user = user(1L);
        LoanApplicationRequestDto request = request();
        when(userReader.getActiveUser(userDetails)).thenReturn(user);
        when(loanProductService.getLoanProduct(request.loanProductId()))
                .thenThrow(new BusinessException(ErrorCode.LOAN_PRODUCT_NOT_FOUNT));

        assertThatThrownBy(() -> loanApplyUseCase.applyLoan(request, userDetails))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.LOAN_PRODUCT_NOT_FOUNT));

        verifyNoInteractions(loanApplicationRepository);
    }

    @DisplayName("입금 계좌 조회 실패는 신청 저장 없이 그대로 전파한다")
    @Test
    void applyLoanPropagatesAccountFailure() {
        FinCoreUserDetails userDetails = userDetails(1L);
        User user = user(1L);
        LoanProduct loanProduct = loanProduct();
        LoanApplicationRequestDto request = request();
        when(userReader.getActiveUser(userDetails)).thenReturn(user);
        when(loanProductService.getLoanProduct(request.loanProductId())).thenReturn(loanProduct);
        when(accountService.findAccountByAccountNumberAndUser(request.disbursementAccount(), user))
                .thenThrow(new BusinessException(ErrorCode.ACCOUNT_NOT_FOUND));

        assertThatThrownBy(() -> loanApplyUseCase.applyLoan(request, userDetails))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ACCOUNT_NOT_FOUND));

        verifyNoInteractions(loanApplicationRepository);
    }

    @DisplayName("신청 조건이 상품 정책에 맞지 않으면 신청 저장 없이 예외를 전파한다")
    @Test
    void applyLoanRejectsInvalidAmount() {
        FinCoreUserDetails userDetails = userDetails(1L);
        User user = user(1L);
        LoanProduct loanProduct = loanProduct();
        Account account = account(user);
        LoanApplicationRequestDto request = new LoanApplicationRequestDto(
                loanProduct.getLoanProductId(),
                account.getAccountNumber(),
                new BigDecimal("999999.00"),
                12,
                new BigDecimal("50000000.00"),
                BigDecimal.ZERO
        );
        when(userReader.getActiveUser(userDetails)).thenReturn(user);
        when(loanProductService.getLoanProduct(request.loanProductId())).thenReturn(loanProduct);
        when(accountService.findAccountByAccountNumberAndUser(request.disbursementAccount(), user)).thenReturn(account);

        assertThatThrownBy(() -> loanApplyUseCase.applyLoan(request, userDetails))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.LOAN_PRODUCT_INVALID_LOAN_AMOUNT));

        verifyNoInteractions(loanApplicationRepository);
    }

    private LoanApplicationRequestDto request() {
        return new LoanApplicationRequestDto(
                1L,
                "110-000-000001",
                new BigDecimal("1000000.00"),
                12,
                new BigDecimal("50000000.00"),
                BigDecimal.ZERO
        );
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

    private Account account(User user) {
        return Account.builder()
                .id(10L)
                .user(user)
                .accountNumber("110-000-000001")
                .accountStatus(AccountStatus.ACTIVE)
                .balance(BigDecimal.ZERO)
                .build();
    }

    private LoanProduct loanProduct() {
        return LoanProduct.builder()
                .loanProductId(1L)
                .productName("Standard Credit Loan")
                .minAmount(new BigDecimal("1000000.00"))
                .maxAmount(new BigDecimal("50000000.00"))
                .baseInterestRate(new BigDecimal("5.20"))
                .overdueInterestRate(new BigDecimal("8.20"))
                .minTermMonths(6)
                .maxTermMonths(36)
                .active(true)
                .build();
    }
}
