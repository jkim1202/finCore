package org.example.fincore.loan.application.entity;

import org.example.fincore.account.entity.Account;
import org.example.fincore.account.entity.AccountStatus;
import org.example.fincore.common.exception.BusinessException;
import org.example.fincore.common.exception.ErrorCode;
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

class LoanApplicationTest {

    @DisplayName("대출 신청은 상품 금액과 기간 범위 안에서 SUBMITTED 상태로 생성된다")
    @Test
    void submitCreatesLoanApplication() {
        User user = user();
        LoanProduct loanProduct = loanProduct();
        Account account = account(user);

        LoanApplication loanApplication = LoanApplication.submit(
                user,
                loanProduct,
                account,
                new BigDecimal("1000000.00"),
                12,
                new BigDecimal("50000000.00"),
                null
        );

        assertThat(loanApplication.getUser()).isEqualTo(user);
        assertThat(loanApplication.getLoanProduct()).isEqualTo(loanProduct);
        assertThat(loanApplication.getDisbursementAccount()).isEqualTo(account);
        assertThat(loanApplication.getRequestedAmount()).isEqualByComparingTo("1000000.00");
        assertThat(loanApplication.getRequestedTermMonths()).isEqualTo(12);
        assertThat(loanApplication.getAnnualIncome()).isEqualByComparingTo("50000000.00");
        assertThat(loanApplication.getExistingDebtAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(loanApplication.getStatus()).isEqualTo(LoanApplicationStatus.SUBMITTED);
    }

    @DisplayName("신청 금액은 상품 최소/최대 금액과 같은 값까지 허용한다")
    @Test
    void submitAllowsBoundaryAmounts() {
        User user = user();
        LoanProduct loanProduct = loanProduct();
        Account account = account(user);

        LoanApplication minAmountApplication = LoanApplication.submit(
                user,
                loanProduct,
                account,
                new BigDecimal("1000000.00"),
                12,
                new BigDecimal("50000000.00"),
                BigDecimal.ZERO
        );
        LoanApplication maxAmountApplication = LoanApplication.submit(
                user,
                loanProduct,
                account,
                new BigDecimal("50000000.00"),
                12,
                new BigDecimal("50000000.00"),
                BigDecimal.ZERO
        );

        assertThat(minAmountApplication.getRequestedAmount()).isEqualByComparingTo("1000000.00");
        assertThat(maxAmountApplication.getRequestedAmount()).isEqualByComparingTo("50000000.00");
    }

    @DisplayName("상품 금액 범위를 벗어나면 대출 신청을 생성하지 않는다")
    @Test
    void submitRejectsInvalidAmount() {
        assertThatThrownBy(() -> LoanApplication.submit(
                user(),
                loanProduct(),
                account(user()),
                new BigDecimal("999999.00"),
                12,
                new BigDecimal("50000000.00"),
                BigDecimal.ZERO
        ))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.LOAN_PRODUCT_INVALID_LOAN_AMOUNT));
    }

    @DisplayName("상품 기간 범위를 벗어나면 대출 신청을 생성하지 않는다")
    @Test
    void submitRejectsInvalidTerm() {
        assertThatThrownBy(() -> LoanApplication.submit(
                user(),
                loanProduct(),
                account(user()),
                new BigDecimal("1000000.00"),
                3,
                new BigDecimal("50000000.00"),
                BigDecimal.ZERO
        ))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.LOAN_PRODUCT_INVALID_LOAN_TERM_MONTHS));
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
