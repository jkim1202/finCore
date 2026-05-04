package org.example.fincore.user.service;

import org.example.fincore.exception.BusinessException;
import org.example.fincore.exception.ErrorCode;
import org.example.fincore.security.FinCoreUserDetails;
import org.example.fincore.user.entity.User;
import org.example.fincore.user.entity.UserRole;
import org.example.fincore.user.entity.UserStatus;
import org.example.fincore.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository);
    }

    @DisplayName("인증 사용자 ID로 활성 사용자를 조회한다")
    @Test
    void findUserByUserDetailsReturnsActiveUser() {
        User user = user(1L, UserStatus.ACTIVE);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User result = userService.findUserByUserDetails(userDetails(1L));

        assertThat(result).isEqualTo(user);
    }

    @DisplayName("인증 사용자 ID에 해당하는 사용자가 없으면 USER_NOT_FOUND 예외를 던진다")
    @Test
    void findUserByUserDetailsRejectsMissingUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findUserByUserDetails(userDetails(1L)))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND));
    }

    @DisplayName("비활성 사용자는 AUTH_USER_STATUS_NOT_ACTIVE 예외로 거부한다")
    @Test
    void findUserByUserDetailsRejectsInactiveUser() {
        User user = user(1L, UserStatus.INACTIVE);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.findUserByUserDetails(userDetails(1L)))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.AUTH_USER_STATUS_NOT_ACTIVE));
    }

    private FinCoreUserDetails userDetails(Long id) {
        return new FinCoreUserDetails(
                id,
                "user@example.com",
                "encoded-password",
                UserStatus.ACTIVE,
                List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER"))
        );
    }

    private User user(Long id, UserStatus status) {
        return User.builder()
                .id(id)
                .email("user@example.com")
                .passwordHash("encoded-password")
                .name("Kim User")
                .phone("010-1234-5678")
                .birthDate(LocalDate.of(2000, 1, 1))
                .status(status)
                .roles(Set.of(UserRole.CUSTOMER))
                .build();
    }
}
