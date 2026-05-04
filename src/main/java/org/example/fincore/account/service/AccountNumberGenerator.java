package org.example.fincore.account.service;

import lombok.AllArgsConstructor;
import org.example.fincore.account.entity.AccountNumberSequence;
import org.example.fincore.account.repository.AccountNumberSequenceRepository;
import org.example.fincore.exception.BusinessException;
import org.example.fincore.exception.ErrorCode;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AccountNumberGenerator {
    private AccountNumberSequenceRepository accountNumberSequenceRepository;

    private static final String BANK_CODE = "110";

    public String generate() {
        try {
            AccountNumberSequence sequence = accountNumberSequenceRepository.save(new AccountNumberSequence());

            long value = sequence.getId();

            return BANK_CODE + "-" +
                    "%03d".formatted(value / 1_000_000) + "-" +
                    "%06d".formatted(value % 1_000_000);
        }
        catch (Exception e) {
            throw new BusinessException(ErrorCode.COMMON_INTERNAL_SERVER_ERROR);
        }
    }
}
