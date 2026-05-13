package org.example.fincore.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    COMMON_INVALID_INPUT(HttpStatus.BAD_REQUEST, "COMMON_001", "유효하지 않은 입력입니다."),
    COMMON_INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_002", "서버 오류가 발생했습니다."),

    AUTH_LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "AUTH_001", "로그인 정보가 일치하지 않습니다."),
    AUTH_INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_002", "유효하지 않은 토큰입니다."),
    AUTH_EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_003", "만료된 토큰입니다."),
    AUTH_UNSUPPORTED_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_004", "지원하지 않는 토큰 타입/구조입니다."),
    AUTH_USER_STATUS_NOT_ACTIVE(HttpStatus.FORBIDDEN, "AUTH_005", "비활성화/삭제된 계정입니다."),

    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_001", "사용자를 찾을 수 없습니다."),
    USER_EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "USER_002", "이미 사용 중인 이메일입니다."),

    ACCOUNT_NOT_FOUND(HttpStatus.CONFLICT, "ACCOUNT_001", "해당 계좌를 찾을 수 없습니다."),
    ACCOUNT_NOT_BELONG_TO_USER(HttpStatus.FORBIDDEN, "ACCOUNT_002", "해당 계좌를 조회할 권한이 없습니다."),
    ACCOUNT_STATUS_NOT_ACTIVE(HttpStatus.FORBIDDEN, "ACCOUNT_003", "해당 계좌는 현재 사용할 수 없습니다."),
    ACCOUNT_BALANCE_NOT_ENOUGH(HttpStatus.BAD_REQUEST, "ACCOUNT_004", "계좌 잔액이 부족합니다."),

    LOAN_PRODUCT_NOT_FOUNT(HttpStatus.NOT_FOUND, "LOAN_PRODUCT_001", "대출 상품을 찾을 수 없습니다."),
    LOAN_PRODUCT_NOT_ACTIVE(HttpStatus.FORBIDDEN, "LOAN_PRODUCT_002", "해당 상품은 현재 비활성 상태입니다."),
    LOAN_PRODUCT_INVALID_LOAN_AMOUNT(HttpStatus.BAD_REQUEST, "LOAN_PRODUCT_003", "신청 금액이 유효하지 않습니다."),
    LOAN_PRODUCT_INVALID_LOAN_TERM_MONTHS(HttpStatus.BAD_REQUEST, "LOAN_PRODUCT_004", "신청 상환 기간이 유효하지 않습니다."),

    LOAN_APPLICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "LOAN_APPLICATION_001", "해당 대출 신청을 찾을 수 없습니다."),
    LOAN_APPLICATION_ACCESS_DENIED(HttpStatus.FORBIDDEN, "LOAN_APPLICATION_002", "해당 대출 신청을 조회할 권한이 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
