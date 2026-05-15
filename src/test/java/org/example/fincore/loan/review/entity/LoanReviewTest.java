package org.example.fincore.loan.review.entity;

import org.example.fincore.account.entity.Account;
import org.example.fincore.account.entity.AccountStatus;
import org.example.fincore.common.exception.BusinessException;
import org.example.fincore.common.exception.ErrorCode;
import org.example.fincore.loan.application.entity.LoanApplication;
import org.example.fincore.loan.application.entity.LoanApplicationStatus;
import org.example.fincore.loan.product.entity.LoanProduct;
import org.example.fincore.user.entity.User;
import org.example.fincore.user.entity.UserRole;
import org.example.fincore.user.entity.UserStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LoanReviewTest {

    @DisplayName("승인 심사 결과를 생성한다")
    @Test
    void approveCreatesApprovedReview() {
        LoanApplication application = loanApplication();

        LoanReview loanReview = LoanReview.approve(
                application,
                new BigDecimal("30000000.00"),
                new BigDecimal("20000000.00"),
                "{\"dti\":0.4}"
        );

        assertThat(loanReview.getApplication()).isEqualTo(application);
        assertThat(loanReview.getDecision()).isEqualTo(LoanReviewDecision.APPROVED);
        assertThat(loanReview.getApprovedLimit()).isEqualByComparingTo("30000000.00");
        assertThat(loanReview.getApprovedAmount()).isEqualByComparingTo("20000000.00");
        assertThat(loanReview.getRejectReason()).isNull();
        assertThat(loanReview.getRuleSnapshot()).isEqualTo("{\"dti\":0.4}");
    }

    @DisplayName("승인 금액이 승인 한도보다 크면 승인 심사 결과를 생성하지 않는다")
    @Test
    void approveRejectsAmountGreaterThanLimit() {
        assertThatThrownBy(() -> LoanReview.approve(
                loanApplication(),
                new BigDecimal("10000000.00"),
                new BigDecimal("20000000.00"),
                "{}"
        ))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.LOAN_REVIEW_INVALID_APPROVED_AMOUNT));
    }

    @DisplayName("거절 심사 결과를 생성한다")
    @Test
    void rejectCreatesRejectedReview() {
        LoanApplication application = loanApplication();

        LoanReview loanReview = LoanReview.reject(
                application,
                BigDecimal.ZERO,
                "DTI exceeds policy limit",
                "{\"dti\":0.9}"
        );

        assertThat(loanReview.getApplication()).isEqualTo(application);
        assertThat(loanReview.getDecision()).isEqualTo(LoanReviewDecision.REJECTED);
        assertThat(loanReview.getApprovedLimit()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(loanReview.getApprovedAmount()).isNull();
        assertThat(loanReview.getRejectReason()).isEqualTo("DTI exceeds policy limit");
        assertThat(loanReview.getRuleSnapshot()).isEqualTo("{\"dti\":0.9}");
    }

    @DisplayName("거절 심사에서 승인 가능 한도는 null일 수 있다")
    @Test
    void rejectAllowsNullApprovedLimit() {
        LoanReview loanReview = LoanReview.reject(
                loanApplication(),
                null,
                "Policy rejected",
                "{}"
        );

        assertThat(loanReview.getDecision()).isEqualTo(LoanReviewDecision.REJECTED);
        assertThat(loanReview.getApprovedLimit()).isNull();
    }

    private LoanApplication loanApplication() {
        User user = user();
        return LoanApplication.builder()
                .application_id(1L)
                .user(user)
                .loanProduct(loanProduct())
                .disbursementAccount(account(user))
                .requestedAmount(new BigDecimal("20000000.00"))
                .requestedTermMonths(24)
                .annualIncome(new BigDecimal("60000000.00"))
                .existingDebtAmount(BigDecimal.ZERO)
                .status(LoanApplicationStatus.SUBMITTED)
                .build();
    }

    private User user() {
        return User.builder()
                .id(1L)
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
