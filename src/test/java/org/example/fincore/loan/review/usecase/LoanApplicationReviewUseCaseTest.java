package org.example.fincore.loan.review.usecase;

import org.example.fincore.account.entity.Account;
import org.example.fincore.account.entity.AccountStatus;
import org.example.fincore.common.exception.BusinessException;
import org.example.fincore.common.exception.ErrorCode;
import org.example.fincore.loan.application.component.LoanApplicationReader;
import org.example.fincore.loan.application.entity.LoanApplication;
import org.example.fincore.loan.application.entity.LoanApplicationStatus;
import org.example.fincore.loan.product.entity.LoanProduct;
import org.example.fincore.loan.review.dto.LoanReviewResponseDto;
import org.example.fincore.loan.review.entity.LoanReview;
import org.example.fincore.loan.review.entity.LoanReviewDecision;
import org.example.fincore.loan.review.repository.LoanReviewRepository;
import org.example.fincore.loan.review.service.CreditLimitCalculator;
import org.example.fincore.loan.review.service.DtiCalculator;
import org.example.fincore.loan.review.service.LoanReviewDecisionPolicy;
import org.example.fincore.loan.review.service.LoanReviewRuleSnapshotWriter;
import org.example.fincore.security.FinCoreUserDetails;
import org.example.fincore.user.component.UserReader;
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
class LoanApplicationReviewUseCaseTest {

    private LoanApplicationReviewUseCase loanApplicationReviewUseCase;

    @Mock
    private UserReader userReader;

    @Mock
    private LoanApplicationReader loanApplicationReader;

    @Mock
    private LoanReviewRepository loanReviewRepository;

    @BeforeEach
    void setUp() {
        loanApplicationReviewUseCase = new LoanApplicationReviewUseCase(
                userReader,
                loanApplicationReader,
                new LoanReviewDecisionPolicy(new DtiCalculator(), new CreditLimitCalculator()),
                new LoanReviewRuleSnapshotWriter(),
                loanReviewRepository
        );
    }

    @DisplayName("ADMIN은 대출 신청을 심사 승인하고 심사 결과를 저장한다")
    @Test
    void reviewLoanApplicationApprovesApplication() {
        FinCoreUserDetails adminDetails = userDetails(99L);
        User admin = user(99L, UserRole.ADMIN);
        LoanApplication application = loanApplication(
                new BigDecimal("20000000.00"),
                new BigDecimal("60000000.00"),
                new BigDecimal("10000000.00")
        );
        when(userReader.getActiveAdmin(adminDetails)).thenReturn(admin);
        when(loanReviewRepository.existsByApplicationId(1L)).thenReturn(false);
        when(loanApplicationReader.getSubmittedForReview(1L)).thenReturn(application);
        when(loanReviewRepository.save(org.mockito.ArgumentMatchers.any(LoanReview.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        LoanReviewResponseDto response = loanApplicationReviewUseCase.reviewLoanApplication(1L, adminDetails);

        ArgumentCaptor<LoanReview> captor = ArgumentCaptor.forClass(LoanReview.class);
        verify(loanReviewRepository).save(captor.capture());
        LoanReview savedReview = captor.getValue();
        assertThat(savedReview.getDecision()).isEqualTo(LoanReviewDecision.APPROVED);
        assertThat(savedReview.getApprovedAmount()).isEqualByComparingTo("20000000.00");
        assertThat(savedReview.getRuleSnapshot()).contains("MVP_2026_05");
        assertThat(application.getStatus()).isEqualTo(LoanApplicationStatus.APPROVED);
        assertThat(application.getReviewedAt()).isNotNull();
        assertThat(response.decision()).isEqualTo(LoanReviewDecision.APPROVED);
        assertThat(response.applicationStatus()).isEqualTo(LoanApplicationStatus.APPROVED);
    }

    @DisplayName("정책 조건을 만족하지 못하면 대출 신청을 거절하고 심사 결과를 저장한다")
    @Test
    void reviewLoanApplicationRejectsApplication() {
        FinCoreUserDetails adminDetails = userDetails(99L);
        User admin = user(99L, UserRole.ADMIN);
        LoanApplication application = loanApplication(
                new BigDecimal("1000000.00"),
                new BigDecimal("50000000.00"),
                new BigDecimal("30000000.00")
        );
        when(userReader.getActiveAdmin(adminDetails)).thenReturn(admin);
        when(loanReviewRepository.existsByApplicationId(1L)).thenReturn(false);
        when(loanApplicationReader.getSubmittedForReview(1L)).thenReturn(application);
        when(loanReviewRepository.save(org.mockito.ArgumentMatchers.any(LoanReview.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        LoanReviewResponseDto response = loanApplicationReviewUseCase.reviewLoanApplication(1L, adminDetails);

        ArgumentCaptor<LoanReview> captor = ArgumentCaptor.forClass(LoanReview.class);
        verify(loanReviewRepository).save(captor.capture());
        LoanReview savedReview = captor.getValue();
        assertThat(savedReview.getDecision()).isEqualTo(LoanReviewDecision.REJECTED);
        assertThat(savedReview.getRejectReason()).isEqualTo("DTI_EXCEEDED");
        assertThat(application.getStatus()).isEqualTo(LoanApplicationStatus.REJECTED);
        assertThat(response.decision()).isEqualTo(LoanReviewDecision.REJECTED);
        assertThat(response.applicationStatus()).isEqualTo(LoanApplicationStatus.REJECTED);
    }

    @DisplayName("이미 심사 결과가 있으면 대출 신청을 다시 심사하지 않는다")
    @Test
    void reviewLoanApplicationRejectsDuplicateReview() {
        FinCoreUserDetails adminDetails = userDetails(99L);
        User admin = user(99L, UserRole.ADMIN);
        when(userReader.getActiveAdmin(adminDetails)).thenReturn(admin);
        when(loanReviewRepository.existsByApplicationId(1L)).thenReturn(true);

        assertThatThrownBy(() -> loanApplicationReviewUseCase.reviewLoanApplication(1L, adminDetails))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.LOAN_REVIEW_ALREADY_EXISTS));

        verifyNoInteractions(loanApplicationReader);
    }

    private FinCoreUserDetails userDetails(Long id) {
        return new FinCoreUserDetails(id, "admin@example.com", "encoded-password", UserStatus.ACTIVE,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }

    private LoanApplication loanApplication(BigDecimal requestedAmount, BigDecimal annualIncome, BigDecimal existingDebtAmount) {
        User user = user(1L, UserRole.CUSTOMER);
        return LoanApplication.builder()
                .application_id(1L)
                .user(user)
                .loanProduct(loanProduct())
                .disbursementAccount(account(user))
                .requestedAmount(requestedAmount)
                .requestedTermMonths(24)
                .annualIncome(annualIncome)
                .existingDebtAmount(existingDebtAmount)
                .status(LoanApplicationStatus.SUBMITTED)
                .build();
    }

    private User user(Long id, UserRole role) {
        return User.builder()
                .id(id)
                .email("user" + id + "@example.com")
                .passwordHash("encoded-password")
                .name("Kim User")
                .phone("010-1234-567" + id)
                .birthDate(LocalDate.of(2000, 1, 1))
                .status(UserStatus.ACTIVE)
                .roles(Set.of(role))
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
