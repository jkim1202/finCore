package org.example.fincore.loan.application.usecase;

import lombok.AllArgsConstructor;
import org.example.fincore.account.entity.Account;
import org.example.fincore.account.service.AccountService;
import org.example.fincore.loan.application.dto.LoanApplicationRequestDto;
import org.example.fincore.loan.application.dto.LoanApplicationResponseDto;
import org.example.fincore.loan.application.entity.LoanApplication;
import org.example.fincore.loan.application.repository.LoanApplicationRepository;
import org.example.fincore.loan.product.entity.LoanProduct;
import org.example.fincore.loan.product.service.LoanProductService;
import org.example.fincore.security.FinCoreUserDetails;
import org.example.fincore.user.entity.User;
import org.example.fincore.user.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class LoanApplyUseCase {
    private final AccountService accountService;
    private final UserService userService;
    private final LoanProductService loanProductService;
    private final LoanApplicationRepository loanApplicationRepository;

    @Transactional
    public LoanApplicationResponseDto applyLoan(LoanApplicationRequestDto requestDto, FinCoreUserDetails userDetails) {
        User user = userService.findUserByUserDetails(userDetails);

        LoanProduct loanProduct = loanProductService.getLoanProduct(requestDto.loanProductId());

        Account account = accountService.findAccountByAccountNumberAndUser(requestDto.disbursementAccount(), user);

        LoanApplication loanApplication = LoanApplication.submit(
                user,
                loanProduct,
                account,
                requestDto.requestedAmount(),
                requestDto.requestedTermMonths(),
                requestDto.annualIncome(),
                requestDto.existingDebtAmount()
        );

        return LoanApplicationResponseDto.from(loanApplicationRepository.save(loanApplication));
    }
}
