package org.example.fincore.loan.review.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class DtiCalculatorTest {

    private final DtiCalculator dtiCalculator = new DtiCalculator();

    @DisplayName("기존 부채를 연소득으로 나눠 DTI를 계산한다")
    @Test
    void calculateDti() {
        BigDecimal dti = dtiCalculator.calculate(
                new BigDecimal("50000000.00"),
                new BigDecimal("10000000.00")
        );

        assertThat(dti).isEqualByComparingTo("0.2000");
    }

    @DisplayName("연소득이 0 이하이면 최대 위험값으로 계산한다")
    @Test
    void calculateDtiWithZeroIncome() {
        BigDecimal dti = dtiCalculator.calculate(BigDecimal.ZERO, BigDecimal.ZERO);

        assertThat(dti).isEqualByComparingTo(BigDecimal.ONE);
    }
}
