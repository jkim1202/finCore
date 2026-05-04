package org.example.fincore.account.repository;

import org.example.fincore.account.entity.AccountNumberSequence;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountNumberSequenceRepository extends JpaRepository<AccountNumberSequence, Long> {
}
