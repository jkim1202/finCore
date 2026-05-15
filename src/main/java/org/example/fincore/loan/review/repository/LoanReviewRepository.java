package org.example.fincore.loan.review.repository;

import org.example.fincore.loan.review.entity.LoanReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LoanReviewRepository extends JpaRepository<LoanReview, Long> {
    @Query("select count(lr) > 0 from LoanReview lr where lr.application.application_id = :applicationId")
    boolean existsByApplicationId(@Param("applicationId") Long applicationId);
}
