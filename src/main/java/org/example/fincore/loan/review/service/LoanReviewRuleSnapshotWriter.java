package org.example.fincore.loan.review.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.fincore.common.exception.BusinessException;
import org.example.fincore.common.exception.ErrorCode;
import org.example.fincore.loan.review.model.LoanReviewEvaluation;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class LoanReviewRuleSnapshotWriter {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String write(LoanReviewEvaluation evaluation) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("policyVersion", evaluation.policy().policyVersion());
        snapshot.put("maxDtiRatio", evaluation.policy().maxDtiRatio());
        snapshot.put("incomeMultiplier", evaluation.policy().incomeMultiplier());
        snapshot.put("maxLimitRatioOfProductMaxAmount", evaluation.policy().maxLimitRatioOfProductMaxAmount());
        snapshot.put("minimumAnnualIncome", evaluation.policy().minimumAnnualIncome());
        snapshot.put("decision", evaluation.decision());
        snapshot.put("approvedLimit", evaluation.approvedLimit());
        snapshot.put("approvedAmount", evaluation.approvedAmount());
        snapshot.put("dti", evaluation.dti());
        snapshot.put("rejectReason", evaluation.rejectReason());

        try {
            return objectMapper.writeValueAsString(snapshot);
        } catch (JsonProcessingException e) {
            throw new BusinessException(ErrorCode.COMMON_INTERNAL_SERVER_ERROR);
        }
    }
}
