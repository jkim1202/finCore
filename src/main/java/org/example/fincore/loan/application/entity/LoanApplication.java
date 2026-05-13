package org.example.fincore.loan.application.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.fincore.account.entity.Account;
import org.example.fincore.common.exception.BusinessException;
import org.example.fincore.common.exception.ErrorCode;
import org.example.fincore.loan.product.entity.LoanProduct;
import org.example.fincore.user.entity.User;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "loan_application")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@ToString
@EntityListeners(AuditingEntityListener.class)
public class LoanApplication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long application_id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_product_id", nullable = false)
    private LoanProduct loanProduct;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "disbursement_account_id", nullable = false)
    private Account disbursementAccount;

    @Column(name = "requested_amount", nullable = false)
    private BigDecimal requestedAmount;

    @Column(name = "requested_term_months", nullable = false)
    private Integer requestedTermMonths;

    @Column(name = "annual_income", nullable = false)
    private BigDecimal annualIncome;

    @Column(name = "existing_debt_amount", nullable = false)
    private BigDecimal existingDebtAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private LoanApplicationStatus status;

    @CreatedDate
    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    public static LoanApplication submit(
            User user,
            LoanProduct loanProduct,
            Account disbursementAccount,
            BigDecimal requestedAmount,
            Integer requestedTermMonths,
            BigDecimal annualIncome,
            BigDecimal existingDebtAmount
    ) {
        validateRequestedAmount(loanProduct, requestedAmount);
        validateRequestedTerm(loanProduct, requestedTermMonths);

        return LoanApplication.builder()
                .user(user)
                .loanProduct(loanProduct)
                .disbursementAccount(disbursementAccount)
                .requestedAmount(requestedAmount)
                .requestedTermMonths(requestedTermMonths)
                .annualIncome(annualIncome)
                .existingDebtAmount(existingDebtAmount == null ? BigDecimal.ZERO : existingDebtAmount)
                .status(LoanApplicationStatus.SUBMITTED)
                .build();
    }

    private static void validateRequestedTerm(LoanProduct loanProduct, Integer requestedTermMonths) {
        if(loanProduct.getMinTermMonths() > requestedTermMonths || loanProduct.getMaxTermMonths() < requestedTermMonths) {
            throw new BusinessException(ErrorCode.LOAN_PRODUCT_INVALID_LOAN_TERM_MONTHS);
        }
    }

    private static void validateRequestedAmount(LoanProduct loanProduct, BigDecimal requestedAmount) {
        if(requestedAmount.compareTo(loanProduct.getMinAmount()) < 0 || requestedAmount.compareTo(loanProduct.getMaxAmount()) > 0){
            throw new BusinessException(ErrorCode.LOAN_PRODUCT_INVALID_LOAN_AMOUNT);
        }
    }
}
