package org.example.fincore.loan.application.component;

import org.example.fincore.account.entity.Account;
import org.example.fincore.account.entity.AccountStatus;
import org.example.fincore.common.exception.BusinessException;
import org.example.fincore.common.exception.ErrorCode;
import org.example.fincore.loan.application.entity.LoanApplication;
import org.example.fincore.loan.application.entity.LoanApplicationStatus;
import org.example.fincore.loan.application.repository.LoanApplicationRepository;
import org.example.fincore.loan.product.entity.LoanProduct;
import org.example.fincore.user.entity.User;
import org.example.fincore.user.entity.UserRole;
import org.example.fincore.user.entity.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoanApplicationReaderTest {

    private LoanApplicationReader loanApplicationReader;

    @Mock
    private LoanApplicationRepository loanApplicationRepository;

    @BeforeEach
    void setUp() {
        loanApplicationReader = new LoanApplicationReader(loanApplicationRepository);
    }

    @DisplayName("신청 ID로 대출 신청을 조회한다")
    @Test
    void getByIdReturnsLoanApplication() {
        LoanApplication loanApplication = loanApplication(user(1L, UserRole.CUSTOMER));
        when(loanApplicationRepository.findById(1L)).thenReturn(Optional.of(loanApplication));

        LoanApplication result = loanApplicationReader.getById(1L);

        assertThat(result).isEqualTo(loanApplication);
    }

    @DisplayName("신청이 없으면 LOAN_APPLICATION_NOT_FOUND 예외를 던진다")
    @Test
    void getByIdRejectsMissingApplication() {
        when(loanApplicationRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> loanApplicationReader.getById(1L))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.LOAN_APPLICATION_NOT_FOUND));
    }

    @DisplayName("신청자는 본인 대출 신청을 조회할 수 있다")
    @Test
    void getReadableByAllowsOwner() {
        User owner = user(1L, UserRole.CUSTOMER);
        LoanApplication loanApplication = loanApplication(owner);
        when(loanApplicationRepository.findById(1L)).thenReturn(Optional.of(loanApplication));

        LoanApplication result = loanApplicationReader.getReadableBy(1L, owner);

        assertThat(result).isEqualTo(loanApplication);
    }

    @DisplayName("ADMIN은 다른 사용자의 대출 신청을 조회할 수 있다")
    @Test
    void getReadableByAllowsAdmin() {
        User owner = user(1L, UserRole.CUSTOMER);
        User admin = user(2L, UserRole.ADMIN);
        LoanApplication loanApplication = loanApplication(owner);
        when(loanApplicationRepository.findById(1L)).thenReturn(Optional.of(loanApplication));

        LoanApplication result = loanApplicationReader.getReadableBy(1L, admin);

        assertThat(result).isEqualTo(loanApplication);
    }

    @DisplayName("본인도 ADMIN도 아니면 대출 신청 조회를 거부한다")
    @Test
    void getReadableByRejectsOtherCustomer() {
        User owner = user(1L, UserRole.CUSTOMER);
        User other = user(2L, UserRole.CUSTOMER);
        LoanApplication loanApplication = loanApplication(owner);
        when(loanApplicationRepository.findById(1L)).thenReturn(Optional.of(loanApplication));

        assertThatThrownBy(() -> loanApplicationReader.getReadableBy(1L, other))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.LOAN_APPLICATION_ACCESS_DENIED));
    }

    private LoanApplication loanApplication(User user) {
        return LoanApplication.builder()
                .application_id(1L)
                .user(user)
                .loanProduct(loanProduct())
                .disbursementAccount(account(user))
                .requestedAmount(new BigDecimal("1000000.00"))
                .requestedTermMonths(12)
                .annualIncome(new BigDecimal("50000000.00"))
                .existingDebtAmount(BigDecimal.ZERO)
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
