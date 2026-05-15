package org.example.fincore.loan.review.service;

import org.example.fincore.account.entity.Account;
import org.example.fincore.account.entity.AccountStatus;
import org.example.fincore.loan.application.entity.LoanApplication;
import org.example.fincore.loan.application.entity.LoanApplicationStatus;
import org.example.fincore.loan.product.entity.LoanProduct;
import org.example.fincore.loan.review.entity.LoanReviewDecision;
import org.example.fincore.loan.review.model.LoanReviewEvaluation;
import org.example.fincore.user.entity.User;
import org.example.fincore.user.entity.UserRole;
import org.example.fincore.user.entity.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class LoanReviewDecisionPolicyTest {

    private LoanReviewDecisionPolicy loanReviewDecisionPolicy;

    @BeforeEach
    void setUp() {
        loanReviewDecisionPolicy = new LoanReviewDecisionPolicy(
                new DtiCalculator(),
                new CreditLimitCalculator()
        );
    }

    @DisplayName("정책 조건을 만족하면 신청 금액을 승인한다")
    @Test
    void evaluateApprovesApplication() {
        LoanReviewEvaluation evaluation = loanReviewDecisionPolicy.evaluate(
                loanApplication(
                        new BigDecimal("20000000.00"),
                        new BigDecimal("60000000.00"),
                        new BigDecimal("10000000.00")
                )
        );

        assertThat(evaluation.decision()).isEqualTo(LoanReviewDecision.APPROVED);
        assertThat(evaluation.approvedAmount()).isEqualByComparingTo("20000000.00");
        assertThat(evaluation.approvedLimit()).isEqualByComparingTo("50000000.000");
        assertThat(evaluation.rejectReason()).isNull();
    }

    @DisplayName("최소 연소득 정책을 만족하지 못하면 거절한다")
    @Test
    void evaluateRejectsMinimumIncomeNotMet() {
        LoanReviewEvaluation evaluation = loanReviewDecisionPolicy.evaluate(
                loanApplication(
                        new BigDecimal("1000000.00"),
                        new BigDecimal("10000000.00"),
                        BigDecimal.ZERO
                )
        );

        assertThat(evaluation.decision()).isEqualTo(LoanReviewDecision.REJECTED);
        assertThat(evaluation.rejectReason()).isEqualTo("MINIMUM_INCOME_NOT_MET");
    }

    @DisplayName("DTI가 정책 한도를 초과하면 거절한다")
    @Test
    void evaluateRejectsDtiExceeded() {
        LoanReviewEvaluation evaluation = loanReviewDecisionPolicy.evaluate(
                loanApplication(
                        new BigDecimal("1000000.00"),
                        new BigDecimal("50000000.00"),
                        new BigDecimal("30000000.00")
                )
        );

        assertThat(evaluation.decision()).isEqualTo(LoanReviewDecision.REJECTED);
        assertThat(evaluation.rejectReason()).isEqualTo("DTI_EXCEEDED");
    }

    @DisplayName("신청 금액이 승인 가능 한도를 초과하면 거절한다")
    @Test
    void evaluateRejectsRequestedAmountExceedsLimit() {
        LoanReviewEvaluation evaluation = loanReviewDecisionPolicy.evaluate(
                loanApplication(
                        new BigDecimal("30000000.00"),
                        new BigDecimal("15000000.00"),
                        BigDecimal.ZERO
                )
        );

        assertThat(evaluation.decision()).isEqualTo(LoanReviewDecision.REJECTED);
        assertThat(evaluation.rejectReason()).isEqualTo("REQUESTED_AMOUNT_EXCEEDS_LIMIT");
        assertThat(evaluation.approvedLimit()).isEqualByComparingTo("22500000.000");
    }

    private LoanApplication loanApplication(BigDecimal requestedAmount, BigDecimal annualIncome, BigDecimal existingDebtAmount) {
        User user = user();
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
