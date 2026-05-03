package org.example.fincore.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    COMMON_INVALID_INPUT(HttpStatus.BAD_REQUEST, "COMMON_001", "유효하지 않은 입력입니다."),

    AUTH_LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "AUTH_001", "로그인 정보가 일치하지 않습니다."),
    AUTH_INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_002", "유효하지 않은 토큰입니다."),
    AUTH_EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_003", "만료된 토큰입니다."),
    AUTH_UNSUPPORTED_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_004", "지원하지 않는 토큰 타입/구조입니다."),
    AUTH_STATUS_NOT_ACTIVE(HttpStatus.FORBIDDEN, "AUTH_005", "비활성화/삭제된 계정입니다."),


    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_001", "사용자를 찾을 수 없습니다."),
    USER_EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "USER_002", "이미 사용 중인 이메일입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
