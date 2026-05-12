package org.example.fincore.account.repository;

import jakarta.persistence.LockModeType;
import org.example.fincore.account.entity.Account;
import org.example.fincore.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Account a where a.id = :accountId")
    Optional<Account> findByIdForUpdate(@Param("accountId") Long accountId);

    Optional<Account> findByAccountNumber(String accountNumber);
}
