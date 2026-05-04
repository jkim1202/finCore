package org.example.fincore.account.repository;

import org.example.fincore.account.entity.AccountTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface AccountTransactionRepository extends JpaRepository<AccountTransaction, Long> {
    Page<AccountTransaction> findByAccountId(
            Long accountId,
            Pageable pageable
    );

    @Query("""
        select t from AccountTransaction t
        join fetch t.account a
        join fetch a.user
        where t.id = :transactionId
    """)
    Optional<AccountTransaction> findByTransactionId(Long transactionId);
}
