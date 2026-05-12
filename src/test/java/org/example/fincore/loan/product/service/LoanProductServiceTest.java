package org.example.fincore.loan.product.service;

import org.example.fincore.loan.product.dto.LoanProductsSearchRequestDto;
import org.example.fincore.loan.product.dto.LoanProductsSearchResponseDto;
import org.example.fincore.loan.product.entity.LoanProduct;
import org.example.fincore.loan.product.repository.LoanProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoanProductServiceTest {

    private LoanProductService loanProductService;

    @Mock
    private LoanProductRepository loanProductRepository;

    @BeforeEach
    void setUp() {
        loanProductService = new LoanProductService(loanProductRepository);
    }

    @DisplayName("대출 상품 목록 조회 시 기본 페이지와 기본 금리 정렬로 활성 상품만 조회한다")
    @Test
    void getLoanProductsUsesDefaultPageAndActiveFilter() {
        LoanProduct product = loanProduct(1L, "Standard Credit Loan", "1000000", "50000000", "5.20");
        when(loanProductRepository.findLoanProductByActive(org.mockito.ArgumentMatchers.any(Pageable.class), eq(true)))
                .thenReturn(new PageImpl<>(List.of(product)));

        Page<LoanProductsSearchResponseDto> result = loanProductService.getLoanProducts(
                new LoanProductsSearchRequestDto(null, null, null)
        );

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(loanProductRepository).findLoanProductByActive(pageableCaptor.capture(), eq(true));
        Pageable pageable = pageableCaptor.getValue();
        assertThat(pageable.getPageNumber()).isZero();
        assertThat(pageable.getPageSize()).isEqualTo(10);
        assertThat(pageable.getSort().getOrderFor("baseInterestRate")).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).loanProductId()).isEqualTo(1L);
        assertThat(result.getContent().get(0).productName()).isEqualTo("Standard Credit Loan");
    }

    @DisplayName("대출 상품 목록 조회 시 최대 한도 오름차순 정렬 요청을 Pageable에 반영한다")
    @Test
    void getLoanProductsUsesMaxAmountAscendingSort() {
        when(loanProductRepository.findLoanProductByActive(org.mockito.ArgumentMatchers.any(Pageable.class), eq(true)))
                .thenReturn(Page.empty());

        loanProductService.getLoanProducts(new LoanProductsSearchRequestDto(1, 5, "MAX AMOUNT ASC"));

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(loanProductRepository).findLoanProductByActive(pageableCaptor.capture(), eq(true));
        Pageable pageable = pageableCaptor.getValue();
        assertThat(pageable.getPageNumber()).isEqualTo(1);
        assertThat(pageable.getPageSize()).isEqualTo(5);
        assertThat(pageable.getSort().getOrderFor("maxAmount")).isNotNull();
    }

    private LoanProduct loanProduct(Long loanProductId, String productName, String minAmount, String maxAmount, String baseInterestRate) {
        return LoanProduct.builder()
                .loanProductId(loanProductId)
                .productName(productName)
                .minAmount(new BigDecimal(minAmount))
                .maxAmount(new BigDecimal(maxAmount))
                .baseInterestRate(new BigDecimal(baseInterestRate))
                .overdueInterestRate(new BigDecimal("8.20"))
                .minTermMonths(6)
                .maxTermMonths(36)
                .active(true)
                .build();
    }
}
