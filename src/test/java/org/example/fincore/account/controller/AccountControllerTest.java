package org.example.fincore.account.controller;

import org.example.fincore.account.dto.AccountCreateResponseDto;
import org.example.fincore.account.dto.AccountDepositResponseDto;
import org.example.fincore.account.dto.AccountDetailResponseDto;
import org.example.fincore.account.dto.AccountWithdrawResponseDto;
import org.example.fincore.account.entity.AccountStatus;
import org.example.fincore.account.entity.TransactionType;
import org.example.fincore.account.usecase.AccountCreateUseCase;
import org.example.fincore.account.usecase.AccountDepositUseCase;
import org.example.fincore.account.usecase.AccountQueryUseCase;
import org.example.fincore.account.usecase.AccountWithdrawUseCase;
import org.example.fincore.exception.GlobalExceptionHandler;
import org.example.fincore.security.FinCoreUserDetails;
import org.example.fincore.user.entity.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.core.MethodParameter;
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
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AccountControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AccountCreateUseCase accountCreateUseCase;

    @Mock
    private AccountQueryUseCase accountQueryUseCase;

    @Mock
    private AccountDepositUseCase accountDepositUseCase;

    @Mock
    private AccountWithdrawUseCase accountWithdrawUseCase;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders
                .standaloneSetup(new AccountController(
                        accountCreateUseCase,
                        accountQueryUseCase,
                        accountDepositUseCase,
                        accountWithdrawUseCase
                ))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .setCustomArgumentResolvers(authenticationPrincipalResolver())
                .build();
    }

    @DisplayName("계좌 생성 요청이 성공하면 생성된 계좌 정보를 201로 반환한다")
    @Test
    void createAccountReturnsCreatedAccount() throws Exception {
        when(accountCreateUseCase.createAccount(nullable(FinCoreUserDetails.class)))
                .thenReturn(new AccountCreateResponseDto(
                        1L,
                        10L,
                        "110-000-000001",
                        AccountStatus.ACTIVE,
                        LocalDateTime.of(2026, 5, 5, 1, 0)
                ));

        mockMvc.perform(post("/api/v1/accounts"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.accountId").value(10))
                .andExpect(jsonPath("$.accountNumber").value("110-000-000001"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        verify(accountCreateUseCase).createAccount(nullable(FinCoreUserDetails.class));
    }

    @DisplayName("계좌 상세 조회 요청이 성공하면 계좌 정보를 200으로 반환한다")
    @Test
    void getAccountReturnsAccountDetail() throws Exception {
        when(accountQueryUseCase.getAccountDetail(eq(10L), nullable(FinCoreUserDetails.class)))
                .thenReturn(new AccountDetailResponseDto(
                        10L,
                        "110-000-000001",
                        AccountStatus.ACTIVE,
                        BigDecimal.valueOf(1_000)
                ));

        mockMvc.perform(get("/api/v1/accounts/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value(10))
                .andExpect(jsonPath("$.accountNumber").value("110-000-000001"))
                .andExpect(jsonPath("$.accountStatus").value("ACTIVE"))
                .andExpect(jsonPath("$.balance").value(1000));
    }

    @DisplayName("입금 요청이 성공하면 거래 결과를 201로 반환한다")
    @Test
    void depositReturnsCreatedTransaction() throws Exception {
        when(accountDepositUseCase.deposit(eq(10L), nullable(FinCoreUserDetails.class), org.mockito.ArgumentMatchers.any()))
                .thenReturn(new AccountDepositResponseDto(
                        10L,
                        "110-000-000001",
                        100L,
                        TransactionType.DEPOSIT,
                        BigDecimal.valueOf(500),
                        BigDecimal.valueOf(1_500),
                        null
                ));

        mockMvc.perform(post("/api/v1/accounts/10/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "amount": 500
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accountId").value(10))
                .andExpect(jsonPath("$.transactionId").value(100))
                .andExpect(jsonPath("$.transactionType").value("DEPOSIT"))
                .andExpect(jsonPath("$.amount").value(500))
                .andExpect(jsonPath("$.balanceAfter").value(1500));
    }

    @DisplayName("입금 금액이 유효하지 않으면 400 공통 입력 오류를 반환한다")
    @Test
    void depositRejectsInvalidAmount() throws Exception {
        mockMvc.perform(post("/api/v1/accounts/10/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "amount": 0
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON_001"));

        verifyNoInteractions(accountDepositUseCase);
    }

    @DisplayName("출금 요청이 성공하면 거래 결과를 201로 반환한다")
    @Test
    void withdrawReturnsCreatedTransaction() throws Exception {
        when(accountWithdrawUseCase.withdraw(eq(10L), nullable(FinCoreUserDetails.class), org.mockito.ArgumentMatchers.any()))
                .thenReturn(new AccountWithdrawResponseDto(
                        10L,
                        "110-000-000001",
                        100L,
                        TransactionType.WITHDRAWAL,
                        BigDecimal.valueOf(500),
                        BigDecimal.valueOf(500),
                        null
                ));

        mockMvc.perform(post("/api/v1/accounts/10/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "amount": 500
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accountId").value(10))
                .andExpect(jsonPath("$.transactionId").value(100))
                .andExpect(jsonPath("$.transactionType").value("WITHDRAWAL"))
                .andExpect(jsonPath("$.amount").value(500))
                .andExpect(jsonPath("$.balanceAfter").value(500));
    }

    @DisplayName("출금 금액이 유효하지 않으면 400 공통 입력 오류를 반환한다")
    @Test
    void withdrawRejectsInvalidAmount() throws Exception {
        mockMvc.perform(post("/api/v1/accounts/10/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "amount": -1
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON_001"));

        verifyNoInteractions(accountWithdrawUseCase);
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
