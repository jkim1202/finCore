package org.example.fincore.account.controller;

import org.example.fincore.account.dto.TransactionViewResponseDto;
import org.example.fincore.account.entity.TransactionType;
import org.example.fincore.account.usecase.AccountQueryUseCase;
import org.example.fincore.common.exception.GlobalExceptionHandler;
import org.example.fincore.security.FinCoreUserDetails;
import org.example.fincore.user.entity.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AccountTransactionControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AccountQueryUseCase accountQueryUseCase;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders
                .standaloneSetup(new AccountTransactionController(accountQueryUseCase))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .setCustomArgumentResolvers(authenticationPrincipalResolver())
                .build();
    }

    @DisplayName("계좌 거래내역 목록 조회가 성공하면 페이징된 거래 목록을 200으로 반환한다")
    @Test
    void getTransactionsReturnsPagedTransactions() throws Exception {
        when(accountQueryUseCase.getTransactions(eq(10L), nullable(FinCoreUserDetails.class), org.mockito.ArgumentMatchers.any()))
                .thenReturn(new PageImpl<>(List.of(response(100L)), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/api/v1/accounts/10/transactions")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].accountId").value(10))
                .andExpect(jsonPath("$.content[0].transactionId").value(100))
                .andExpect(jsonPath("$.content[0].transactionType").value("DEPOSIT"));

        verify(accountQueryUseCase).getTransactions(eq(10L), nullable(FinCoreUserDetails.class), org.mockito.ArgumentMatchers.any());
    }

    @DisplayName("거래 단건 조회가 성공하면 거래 상세 정보를 200으로 반환한다")
    @Test
    void getTransactionReturnsTransaction() throws Exception {
        when(accountQueryUseCase.getTransaction(eq(100L), nullable(FinCoreUserDetails.class)))
                .thenReturn(response(100L));

        mockMvc.perform(get("/api/v1/accounts/transactions/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value(10))
                .andExpect(jsonPath("$.transactionId").value(100))
                .andExpect(jsonPath("$.transactionType").value("DEPOSIT"));
    }

    private TransactionViewResponseDto response(Long transactionId) {
        return new TransactionViewResponseDto(
                10L,
                "110-000-000001",
                transactionId,
                TransactionType.DEPOSIT,
                BigDecimal.valueOf(500),
                BigDecimal.valueOf(1_500),
                null
        );
    }

    private HandlerMethodArgumentResolver authenticationPrincipalResolver() {
        return new HandlerMethodArgumentResolver() {
            @Override
            public boolean supportsParameter(MethodParameter parameter) {
                return parameter.hasParameterAnnotation(AuthenticationPrincipal.class);
            }

            @Override
            public Object resolveArgument(
                    MethodParameter parameter,
                    ModelAndViewContainer mavContainer,
                    NativeWebRequest webRequest,
                    WebDataBinderFactory binderFactory
            ) {
                return new FinCoreUserDetails(
                        1L,
                        "user@example.com",
                        "encoded-password",
                        UserStatus.ACTIVE,
                        List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER"))
                );
            }
        };
    }
}
