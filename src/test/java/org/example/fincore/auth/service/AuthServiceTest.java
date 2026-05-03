package org.example.fincore.auth.service;

import org.example.fincore.auth.dto.LoginRequestDto;
import org.example.fincore.auth.dto.LoginResponseDto;
import org.example.fincore.auth.dto.RefreshRequestDto;
import org.example.fincore.auth.dto.RefreshResponseDto;
import org.example.fincore.auth.dto.RegisterRequestDto;
import org.example.fincore.auth.dto.RegisterResponseDto;
import org.example.fincore.exception.BusinessException;
import org.example.fincore.exception.ErrorCode;
import org.example.fincore.security.FinCoreUserDetails;
import org.example.fincore.security.JwtTokenProvider;
import org.example.fincore.user.entity.User;
import org.example.fincore.user.entity.UserRole;
import org.example.fincore.user.entity.UserStatus;
import org.example.fincore.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private AuthService authService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserDetailsService userDetailsService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
                userRepository,
                passwordEncoder,
                jwtTokenProvider,
                authenticationManager,
                userDetailsService
        );
    }

    @Test
    void registerCreatesActiveCustomerUserWithEncodedPassword() {
        RegisterRequestDto request = new RegisterRequestDto(
                "user@example.com",
                "raw-password",
                "010-1234-5678",
                "Kim User",
                LocalDate.of(2000, 1, 1)
        );
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(request.password())).thenReturn("encoded-password");

        RegisterResponseDto response = authService.register(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getEmail()).isEqualTo(request.email());
        assertThat(savedUser.getPasswordHash()).isEqualTo("encoded-password");
        assertThat(savedUser.getName()).isEqualTo(request.name());
        assertThat(savedUser.getPhone()).isEqualTo(request.phone());
        assertThat(savedUser.getBirthDate()).isEqualTo(request.birthDate());
        assertThat(savedUser.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(savedUser.getRoles()).containsExactly(UserRole.CUSTOMER);
        assertThat(response.email()).isEqualTo(request.email());
        assertThat(response.userStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    void registerRejectsDuplicatedEmail() {
        RegisterRequestDto request = new RegisterRequestDto(
                "user@example.com",
                "raw-password",
                "010-1234-5678",
                "Kim User",
                LocalDate.of(2000, 1, 1)
        );
        when(userRepository.findByEmail(request.email()))
                .thenReturn(Optional.of(user(request.email(), UserStatus.ACTIVE)));

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.USER_EMAIL_ALREADY_EXISTS));
    }

    @Test
    void loginAuthenticatesAndReturnsTokens() {
        LoginRequestDto request = new LoginRequestDto("user@example.com", "raw-password");
        UserDetails userDetails = userDetails(request.email(), UserStatus.ACTIVE);
        when(authenticationManager.authenticate(any()))
                .thenReturn(new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        when(userDetailsService.loadUserByUsername(request.email())).thenReturn(userDetails);
        when(jwtTokenProvider.generateAccessToken(userDetails)).thenReturn("access-token");
        when(jwtTokenProvider.generateRefreshToken(userDetails)).thenReturn("refresh-token");

        LoginResponseDto response = authService.login(request);

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
    }

    @Test
    void loginRejectsBadCredentials() {
        LoginRequestDto request = new LoginRequestDto("user@example.com", "wrong-password");
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("bad credentials"));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.AUTH_LOGIN_FAILED));
    }

    @Test
    void loginRejectsInactiveUser() {
        LoginRequestDto request = new LoginRequestDto("user@example.com", "raw-password");
        UserDetails userDetails = userDetails(request.email(), UserStatus.INACTIVE);
        when(authenticationManager.authenticate(any()))
                .thenReturn(new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        when(userDetailsService.loadUserByUsername(request.email())).thenReturn(userDetails);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.AUTH_STATUS_NOT_ACTIVE));
    }

    @Test
    void refreshTokenValidatesRefreshTokenAndReturnsNewTokens() {
        RefreshRequestDto request = new RefreshRequestDto("refresh-token");
        UserDetails userDetails = userDetails("user@example.com", UserStatus.ACTIVE);
        when(jwtTokenProvider.getEmailFromRefreshToken(request.refreshToken())).thenReturn("user@example.com");
        when(userDetailsService.loadUserByUsername("user@example.com")).thenReturn(userDetails);
        when(jwtTokenProvider.validateRefreshToken(request.refreshToken(), userDetails)).thenReturn(true);
        when(jwtTokenProvider.generateAccessToken(userDetails)).thenReturn("new-access-token");
        when(jwtTokenProvider.generateRefreshToken(userDetails)).thenReturn("new-refresh-token");

        RefreshResponseDto response = authService.refreshToken(request);

        assertThat(response.accessToken()).isEqualTo("new-access-token");
        assertThat(response.refreshToken()).isEqualTo("new-refresh-token");
    }

    @Test
    void refreshTokenRejectsInvalidRefreshToken() {
        RefreshRequestDto request = new RefreshRequestDto("refresh-token");
        UserDetails userDetails = userDetails("user@example.com", UserStatus.ACTIVE);
        when(jwtTokenProvider.getEmailFromRefreshToken(request.refreshToken())).thenReturn("user@example.com");
        when(userDetailsService.loadUserByUsername("user@example.com")).thenReturn(userDetails);
        when(jwtTokenProvider.validateRefreshToken(request.refreshToken(), userDetails)).thenReturn(false);

        assertThatThrownBy(() -> authService.refreshToken(request))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.AUTH_INVALID_TOKEN));
    }

    private User user(String email, UserStatus status) {
        return User.builder()
                .id(1L)
                .email(email)
                .passwordHash("encoded-password")
                .name("Kim User")
                .phone("010-1234-5678")
                .birthDate(LocalDate.of(2000, 1, 1))
                .status(status)
                .roles(Set.of(UserRole.CUSTOMER))
                .build();
    }

    private UserDetails userDetails(String email, UserStatus status) {
        return new FinCoreUserDetails(
                1L,
                email,
                "encoded-password",
                status,
                List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER"))
        );
    }
}
