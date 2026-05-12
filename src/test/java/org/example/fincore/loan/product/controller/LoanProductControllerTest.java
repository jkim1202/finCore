package org.example.fincore.loan.product.controller;

import org.example.fincore.common.exception.GlobalExceptionHandler;
import org.example.fincore.loan.product.dto.LoanProductsSearchRequestDto;
import org.example.fincore.loan.product.dto.LoanProductsSearchResponseDto;
import org.example.fincore.loan.product.service.LoanProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class LoanProductControllerTest {

    private MockMvc mockMvc;

    @Mock
    private LoanProductService loanProductService;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders
                .standaloneSetup(new LoanProductController(loanProductService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @DisplayName("대출 상품 목록 조회 요청이 성공하면 활성 상품 목록을 200으로 반환한다")
    @Test
    void getLoanProductsReturnsActiveProducts() throws Exception {
        when(loanProductService.getLoanProducts(any(LoanProductsSearchRequestDto.class)))
                .thenReturn(new PageImpl<>(List.of(response()), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/v1/loan-products")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].loanProductId").value(1))
                .andExpect(jsonPath("$.content[0].productName").value("Standard Credit Loan"))
                .andExpect(jsonPath("$.content[0].minAmount").value(1000000))
                .andExpect(jsonPath("$.content[0].maxAmount").value(50000000))
                .andExpect(jsonPath("$.content[0].baseInterestRate").value(5.20))
                .andExpect(jsonPath("$.content[0].overdueInterestRate").value(8.20))
                .andExpect(jsonPath("$.content[0].minTermMonths").value(6))
                .andExpect(jsonPath("$.content[0].maxTermMonths").value(36))
                .andExpect(jsonPath("$.content[0].active").value(true));

        verify(loanProductService).getLoanProducts(any(LoanProductsSearchRequestDto.class));
    }

    @DisplayName("대출 상품 목록 조회 요청에서 page와 size를 생략해도 컨트롤러는 서비스로 위임한다")
    @Test
    void getLoanProductsAcceptsMissingPagingParameters() throws Exception {
        when(loanProductService.getLoanProducts(any(LoanProductsSearchRequestDto.class)))
                .thenReturn(new PageImpl<>(List.of(response()), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/v1/loan-products"))
                .andExpect(status().isOk());

        verify(loanProductService).getLoanProducts(any(LoanProductsSearchRequestDto.class));
    }

    @DisplayName("대출 상품 목록 조회 요청의 size가 허용 범위를 초과하면 400 공통 입력 오류를 반환한다")
    @Test
    void getLoanProductsRejectsInvalidSize() throws Exception {
        mockMvc.perform(get("/api/v1/loan-products")
                        .param("page", "0")
                        .param("size", "11"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON_001"));

        verifyNoInteractions(loanProductService);
    }

    private LoanProductsSearchResponseDto response() {
        return new LoanProductsSearchResponseDto(
                1L,
                "Standard Credit Loan",
                new BigDecimal("1000000.00"),
                new BigDecimal("50000000.00"),
                new BigDecimal("5.20"),
                new BigDecimal("8.20"),
                6,
                36,
                true
        );
    }
}
