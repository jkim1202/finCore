package org.example.fincore.loan.product.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Table(name = "loan_product")
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
@EntityListeners(AuditingEntityListener.class)
public class LoanProduct {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "loan_product_id", nullable = false)
    private Long loanProductId;

    @Column(name = "product_name", length = 100, nullable = false)
    private String productName;

    @Column(name = "min_amount",  nullable = false)
    private BigDecimal minAmount;

    @Column(name = "max_amount",  nullable = false)
    private BigDecimal maxAmount;

    @Column(name = "base_interest_rate", nullable = false)
    private BigDecimal baseInterestRate;

    @Column(name = "overdue_interest_rate", nullable = false)
    private BigDecimal overdueInterestRate;

    @Column(name = "min_term_months", nullable = false)
    private Integer minTermMonths;

    @Column(name = "max_term_months", nullable = false)
    private Integer maxTermMonths;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private Boolean active = true;

    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
