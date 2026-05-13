package org.example.fincore.loan.application.component;

import lombok.RequiredArgsConstructor;
import org.example.fincore.common.exception.BusinessException;
import org.example.fincore.common.exception.ErrorCode;
import org.example.fincore.loan.application.entity.LoanApplication;
import org.example.fincore.loan.application.repository.LoanApplicationRepository;
import org.example.fincore.user.entity.User;
import org.example.fincore.user.entity.UserRole;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LoanApplicationReader {
    private final LoanApplicationRepository loanApplicationRepository;

    public LoanApplication getById(Long applicationId) {
        return loanApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.LOAN_APPLICATION_NOT_FOUND));
    }

    public LoanApplication getReadableBy(Long applicationId, User user) {
        LoanApplication loanApplication = getById(applicationId);
        validateReadableBy(loanApplication, user);
        return loanApplication;
    }

    private void validateReadableBy(LoanApplication loanApplication, User user) {
        boolean owner = loanApplication.getUser().getId().equals(user.getId());
        boolean admin = user.getRoles().contains(UserRole.ADMIN);

        if (!owner && !admin) {
            throw new BusinessException(ErrorCode.LOAN_APPLICATION_ACCESS_DENIED);
        }
    }
}
