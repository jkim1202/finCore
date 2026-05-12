package org.example.fincore.loan.product.repository;

import org.example.fincore.loan.product.entity.LoanProduct;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoanProductRepository extends JpaRepository<LoanProduct, Long> {
    Page<LoanProduct> findLoanProductByActive(Pageable pageable, Boolean active);
}
