package org.example.fincore.loan.application.repository;

import org.example.fincore.loan.application.entity.LoanApplication;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoanApplicationRepository extends JpaRepository<LoanApplication, Long> {
}
