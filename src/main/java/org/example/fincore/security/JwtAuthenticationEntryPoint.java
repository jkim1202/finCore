package org.example.fincore.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.fincore.common.exception.ErrorCode;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 인증 실패 혹은 미인증 접근 발생 시 HTTP 응답을 만들어 준다.
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        ErrorCode ec = (request.getAttribute("errorCode") instanceof ErrorCode c)
                ? c
                : ErrorCode.AUTH_INVALID_TOKEN;
        Map<String, Object> responseBody = Map.of(
                "status", ec.getStatus().value(),
                "code", ec.getCode(),
                "message", ec.getMessage());

        response.getWriter().write(objectMapper.writeValueAsString(responseBody));
    }
}
