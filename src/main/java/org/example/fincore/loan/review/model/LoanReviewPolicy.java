package org.example.fincore.loan.review.model;

import java.math.BigDecimal;

/**
 * 대출 심사 룰에 사용하는 정책 파라미터입니다.
 * MVP에서는 코드 상수로 관리하고, 운영 단계에서는 DB/설정 파일 기반으로 분리할 수 있습니다.
 */
public record LoanReviewPolicy(
        // 정책 버전은 심사 결과 스냅샷에 남겨 같은 신청을 어떤 기준으로 판단했는지 추적합니다.
        String policyVersion,
        // 최대 허용 DTI 비율입니다. 0.40은 기존 부채가 연소득의 40%를 넘으면 거절한다는 의미입니다.
        BigDecimal maxDtiRatio,
        // 연소득 기반 한도 배수입니다. 예: 연소득 5천만원 * 1.5 = 7천5백만원.
        BigDecimal incomeMultiplier,
        // 상품 최대 한도 중 심사에서 사용할 비율입니다. 1.0이면 상품 최대 한도를 그대로 사용합니다.
        BigDecimal maxLimitRatioOfProductMaxAmount,
        // 최소 연소득 기준입니다. 이 금액 미만이면 다른 조건과 무관하게 거절합니다.
        BigDecimal minimumAnnualIncome
) {
    public static LoanReviewPolicy defaultPolicy() {
        return new LoanReviewPolicy(
                "MVP_2026_05",
                new BigDecimal("0.40"),
                new BigDecimal("1.5"),
                new BigDecimal("1.0"),
                new BigDecimal("12000000")
        );
    }
}
