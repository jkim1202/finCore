package org.example.fincore.loan.application.usecase;

import org.example.fincore.account.entity.Account;
import org.example.fincore.account.entity.AccountStatus;
import org.example.fincore.loan.application.component.LoanApplicationReader;
import org.example.fincore.loan.application.dto.LoanApplicationSearchResponseDto;
import org.example.fincore.loan.application.entity.LoanApplication;
import org.example.fincore.loan.application.entity.LoanApplicationStatus;
import org.example.fincore.loan.product.entity.LoanProduct;
import org.example.fincore.security.FinCoreUserDetails;
import org.example.fincore.user.component.UserReader;
import org.example.fincore.user.entity.User;
import org.example.fincore.user.entity.UserRole;
import org.example.fincore.user.entity.UserStatus;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoanApplicationSearchUseCaseTest {

    private LoanApplicationSearchUseCase loanApplicationSearchUseCase;

    @Mock
    private LoanApplicationReader loanApplicationReader;

    @Mock
    private UserReader userReader;

    @BeforeEach
    void setUp() {
        loanApplicationSearchUseCase = new LoanApplicationSearchUseCase(loanApplicationReader, userReader);
    }

    @DisplayName("인증 사용자로 읽기 가능한 대출 신청을 조회한다")
    @Test
    void searchLoanApplicationReturnsReadableApplication() {
        FinCoreUserDetails userDetails = userDetails(1L);
        User user = user(1L);
        LoanApplication loanApplication = loanApplication(user);
        when(userReader.getActiveUser(userDetails)).thenReturn(user);
        when(loanApplicationReader.getReadableBy(1L, user)).thenReturn(loanApplication);

        LoanApplicationSearchResponseDto response =
                loanApplicationSearchUseCase.searchLoanApplication(1L, userDetails);

        assertThat(response.applicationId()).isEqualTo(1L);
        assertThat(response.userId()).isEqualTo(user.getId());
        assertThat(response.userName()).isEqualTo(user.getName());
        assertThat(response.loanProductId()).isEqualTo(1L);
        assertThat(response.loanProductName()).isEqualTo("Standard Credit Loan");
        assertThat(response.disbursementAccountNumber()).isEqualTo("110-000-000001");
        assertThat(response.status()).isEqualTo(LoanApplicationStatus.SUBMITTED);
    }

    private FinCoreUserDetails userDetails(Long id) {
        return new FinCoreUserDetails(id, "user@example.com", "encoded-password", UserStatus.ACTIVE,
                List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER")));
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
