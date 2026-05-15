package org.example.fincore.user.component;

import lombok.RequiredArgsConstructor;
import org.example.fincore.common.exception.BusinessException;
import org.example.fincore.common.exception.ErrorCode;
import org.example.fincore.security.FinCoreUserDetails;
import org.example.fincore.user.entity.User;
import org.example.fincore.user.entity.UserRole;
import org.example.fincore.user.entity.UserStatus;
import org.example.fincore.user.repository.UserRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserReader {

    private final UserRepository userRepository;

    public User getActiveUser(FinCoreUserDetails userDetails) {
        User user = getById(userDetails.getId());
        validateActive(user);
        return user;
    }

    public User getActiveAdmin(FinCoreUserDetails userDetails) {
        User user = getActiveUser(userDetails);

        if (!user.getRoles().contains(UserRole.ADMIN)) {
            throw new BusinessException(ErrorCode.AUTH_ACCESS_DENIED);
        }

        return user;
    }

    private User getById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private void validateActive(User user) {
        if (UserStatus.INACTIVE.equals(user.getStatus())) {
            throw new BusinessException(ErrorCode.AUTH_USER_STATUS_NOT_ACTIVE);
        }
    }
}
