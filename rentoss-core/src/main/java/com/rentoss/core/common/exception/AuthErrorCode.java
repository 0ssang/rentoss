package com.rentoss.core.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements ErrorCode {

    INVALID_TOKEN(HttpStatus.BAD_REQUEST, "A001", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.BAD_REQUEST, "A002", "유효기간이 만료된 토큰입니다."),
    TOKEN_NOT_FOUND(HttpStatus.BAD_REQUEST, "A003", "토큰을 찾을 수 없습니다."),
    UNSUPPORTED_TOKEN(HttpStatus.BAD_REQUEST, "A004", "지원하지 않는 토큰 형식입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
