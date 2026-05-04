package org.example.fincore.auth.controller;

import org.example.fincore.auth.dto.LoginResponseDto;
import org.example.fincore.auth.dto.RefreshResponseDto;
import org.example.fincore.auth.dto.RegisterResponseDto;
import org.example.fincore.auth.service.AuthService;
import org.example.fincore.exception.GlobalExceptionHandler;
import org.example.fincore.user.entity.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthService authService;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(new AuthController(authService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @DisplayName("회원가입 요청이 성공하면 생성된 사용자 이메일과 상태를 반환한다")
    @Test
    void registerReturnsCreatedUser() throws Exception {
        when(authService.register(any()))
                .thenReturn(new RegisterResponseDto("user@example.com", UserStatus.ACTIVE));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "user@example.com",
                                  "password": "password123!",
                                  "phone": "010-1234-5678",
                                  "name": "Kim User",
                                  "birthDate": "2000-01-01"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("user@example.com"))
                .andExpect(jsonPath("$.userStatus").value("ACTIVE"));

        verify(authService).register(any());
    }

    @DisplayName("로그인 요청이 성공하면 access token과 refresh token을 반환한다")
    @Test
    void loginReturnsTokens() throws Exception {
        when(authService.login(any()))
                .thenReturn(new LoginResponseDto("access-token", "refresh-token"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "user@example.com",
                                  "password": "password123!"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"));

        verify(authService).login(any());
    }

    @DisplayName("토큰 재발급 요청이 성공하면 새 access token과 refresh token을 반환한다")
    @Test
    void refreshReturnsNewTokens() throws Exception {
        when(authService.refreshToken(any()))
                .thenReturn(new RefreshResponseDto("new-access-token", "new-refresh-token"));

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "refreshToken": "refresh-token"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-access-token"))
                .andExpect(jsonPath("$.refreshToken").value("new-refresh-token"));

        verify(authService).refreshToken(any());
    }

    @DisplayName("회원가입 요청 값이 유효하지 않으면 400 공통 입력 오류를 반환한다")
    @Test
    void registerRejectsInvalidRequest() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "not-email",
                                  "password": "",
                                  "phone": "",
                                  "name": "",
                                  "birthDate": null
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON_001"));
    }
}
