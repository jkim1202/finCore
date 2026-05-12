package org.example.fincore.user.service;

import lombok.AllArgsConstructor;
import org.example.fincore.common.exception.BusinessException;
import org.example.fincore.common.exception.ErrorCode;
import org.example.fincore.security.FinCoreUserDetails;
import org.example.fincore.user.entity.User;
import org.example.fincore.user.entity.UserStatus;
import org.example.fincore.user.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserService {
    final UserRepository userRepository;

    public User findUserByUserDetails(FinCoreUserDetails userDetails) {
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        validateUserStatus(user);

        return user;
    }

    private void validateUserStatus(User user){
        if(UserStatus.INACTIVE.equals(user.getStatus())){
            throw new BusinessException(ErrorCode.AUTH_USER_STATUS_NOT_ACTIVE);
        }
    }
}
