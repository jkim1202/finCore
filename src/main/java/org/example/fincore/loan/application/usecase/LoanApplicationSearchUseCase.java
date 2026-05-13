package org.example.fincore.loan.application.usecase;

import lombok.AllArgsConstructor;
import org.example.fincore.loan.application.component.LoanApplicationReader;
import org.example.fincore.loan.application.dto.LoanApplicationSearchResponseDto;
import org.example.fincore.loan.application.entity.LoanApplication;
import org.example.fincore.security.FinCoreUserDetails;
import org.example.fincore.user.component.UserReader;
import org.example.fincore.user.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class LoanApplicationSearchUseCase {
    private final LoanApplicationReader loanApplicationReader;
    private final UserReader userReader;

    @Transactional(readOnly = true)
    public LoanApplicationSearchResponseDto searchLoanApplication(
            Long applicationId,
            FinCoreUserDetails finCoreUserDetails
    ){
        User user = userReader.getActiveUser(finCoreUserDetails);
        LoanApplication loanApplication = loanApplicationReader.getReadableBy(applicationId, user);
        return LoanApplicationSearchResponseDto.from(loanApplication);
    }
}
