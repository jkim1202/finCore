package org.example.fincore.account.service;

import org.example.fincore.account.entity.AccountNumberSequence;
import org.example.fincore.account.repository.AccountNumberSequenceRepository;
import org.example.fincore.common.exception.BusinessException;
import org.example.fincore.common.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountNumberGeneratorTest {

    private AccountNumberGenerator accountNumberGenerator;

    @Mock
    private AccountNumberSequenceRepository accountNumberSequenceRepository;

    @BeforeEach
    void setUp() {
        accountNumberGenerator = new AccountNumberGenerator(accountNumberSequenceRepository);
    }

    @DisplayName("계좌번호 sequence ID를 은행코드 포함 계좌번호 형식으로 변환한다")
    @Test
    void generateReturnsFormattedAccountNumber() {
        when(accountNumberSequenceRepository.save(any(AccountNumberSequence.class)))
                .thenReturn(sequence(1_234_567L));

        String accountNumber = accountNumberGenerator.generate();

        assertThat(accountNumber).isEqualTo("110-001-234567");
    }

    @DisplayName("계좌번호 생성 중 저장소 오류가 발생하면 공통 서버 오류로 변환한다")
    @Test
    void generateWrapsRepositoryFailure() {
        when(accountNumberSequenceRepository.save(any(AccountNumberSequence.class)))
                .thenThrow(new RuntimeException("db error"));

        assertThatThrownBy(() -> accountNumberGenerator.generate())
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.COMMON_INTERNAL_SERVER_ERROR));
    }

    private AccountNumberSequence sequence(Long id) {
        AccountNumberSequence sequence = new AccountNumberSequence();
        ReflectionTestUtils.setField(sequence, "id", id);
        return sequence;
    }
}
