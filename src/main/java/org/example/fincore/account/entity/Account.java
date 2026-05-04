package org.example.fincore.account.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.fincore.exception.BusinessException;
import org.example.fincore.exception.ErrorCode;
import org.example.fincore.user.entity.User;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "accounts",
        uniqueConstraints = {@UniqueConstraint(
                name = "uk_account_number",
                columnNames = "account_number"
        )})
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@ToString
@EntityListeners(AuditingEntityListener.class)
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @Column(name = "account_number", nullable = false)
    private String accountNumber;

    @Column(name = "account_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private AccountStatus accountStatus;

    @Column(name = "balance", nullable = false)
    private BigDecimal balance;

    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public void deposit(BigDecimal amount){
        this.balance = this.balance.add(amount);
    }
    public void withdraw(BigDecimal amount){
        if(this.balance.subtract(amount).compareTo(BigDecimal.ZERO) < 0){
            throw new BusinessException(ErrorCode.ACCOUNT_BALANCE_NOT_ENOUGH);
        }
        this.balance = this.balance.subtract(amount);
    }
}
