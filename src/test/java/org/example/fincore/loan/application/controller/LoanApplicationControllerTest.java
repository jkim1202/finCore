package org.example.fincore.loan.application.controller;

import org.example.fincore.common.exception.GlobalExceptionHandler;
import org.example.fincore.loan.application.dto.LoanApplicationResponseDto;
import org.example.fincore.loan.application.dto.LoanApplicationSearchResponseDto;
import org.example.fincore.loan.application.entity.LoanApplicationStatus;
import org.example.fincore.loan.application.usecase.LoanApplicationSearchUseCase;
import org.example.fincore.loan.application.usecase.LoanApplyUseCase;
import org.example.fincore.security.FinCoreUserDetails;
import org.example.fincore.user.entity.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class LoanApplicationControllerTest {

    private MockMvc mockMvc;

    @Mock
    private LoanApplyUseCase loanApplyUseCase;

    @Mock
    private LoanApplicationSearchUseCase loanApplicationSearchUseCase;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders
                .standaloneSetup(new LoanApplicationController(loanApplyUseCase, loanApplicationSearchUseCase))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .setCustomArgumentResolvers(authenticationPrincipalResolver())
                .build();
    }

    @DisplayName("대출 신청 요청이 성공하면 생성된 신청 정보를 201로 반환한다")
    @Test
    void createLoanApplicationReturnsCreatedApplication() throws Exception {
        when(loanApplyUseCase.applyLoan(any(), nullable(FinCoreUserDetails.class)))
                .thenReturn(new LoanApplicationResponseDto(
                        "Kim User",
                        "Standard Credit Loan",
                        "110-000-000001",
                        new BigDecimal("1000000.00"),
                        12,
                        LoanApplicationStatus.SUBMITTED,
                        LocalDateTime.of(2026, 5, 13, 1, 0)
                ));

        mockMvc.perform(post("/api/v1/loan-applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "loanProductId": 1,
                                  "disbursementAccount": "110-000-000001",
                                  "requestedAmount": 1000000,
                                  "requestedTermMonths": 12,
                                  "annualIncome": 50000000,
                                  "existingDebtAmount": 0
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userName").value("Kim User"))
                .andExpect(jsonPath("$.loanProductName").value("Standard Credit Loan"))
                .andExpect(jsonPath("$.disbursementAccountNumber").value("110-000-000001"))
                .andExpect(jsonPath("$.requestedAmount").value(1000000))
                .andExpect(jsonPath("$.requestedTermMonths").value(12))
                .andExpect(jsonPath("$.status").value("SUBMITTED"));

        verify(loanApplyUseCase).applyLoan(any(), nullable(FinCoreUserDetails.class));
    }

    @DisplayName("대출 신청 필수값이 누락되면 400 공통 입력 오류를 반환한다")
    @Test
    void createLoanApplicationRejectsMissingRequiredValue() throws Exception {
        mockMvc.perform(post("/api/v1/loan-applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "loanProductId": 1,
                                  "disbursementAccount": "110-000-000001",
                                  "requestedTermMonths": 12,
                                  "annualIncome": 50000000
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON_001"));

        verifyNoInteractions(loanApplyUseCase);
    }

    @DisplayName("대출 신청 조회 요청이 성공하면 신청 상세 정보를 200으로 반환한다")
    @Test
    void getLoanApplicationReturnsApplicationDetail() throws Exception {
        when(loanApplicationSearchUseCase.searchLoanApplication(
                org.mockito.ArgumentMatchers.eq(1L),
                nullable(FinCoreUserDetails.class)
        ))
                .thenReturn(new LoanApplicationSearchResponseDto(
                        1L,
                        1L,
                        "Kim User",
                        1L,
                        "Standard Credit Loan",
                        "110-000-000001",
                        new BigDecimal("1000000.00"),
                        12,
                        new BigDecimal("50000000.00"),
                        LoanApplicationStatus.SUBMITTED,
                        LocalDateTime.of(2026, 5, 13, 1, 0),
                        null
                ));

        mockMvc.perform(get("/api/v1/loan-applications/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.applicationId").value(1))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.userName").value("Kim User"))
                .andExpect(jsonPath("$.loanProductId").value(1))
                .andExpect(jsonPath("$.loanProductName").value("Standard Credit Loan"))
                .andExpect(jsonPath("$.disbursementAccountNumber").value("110-000-000001"))
                .andExpect(jsonPath("$.requestedAmount").value(1000000))
                .andExpect(jsonPath("$.requestedTermMonths").value(12))
                .andExpect(jsonPath("$.annualIncome").value(50000000))
                .andExpect(jsonPath("$.status").value("SUBMITTED"));
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
