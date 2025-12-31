package com.rentoss.user.exception;

import com.rentoss.core.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements ErrorCode {

    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "사용자를 찾을 수 없습니다."),
    DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "U002", "이미 사용중인 닉네임 입니다."),
    ALREADY_WITHDRAWN(HttpStatus.BAD_REQUEST, "U003", "이미 탈퇴한 사용자 입니다."),
    USER_NOT_ACTIVE(HttpStatus.FORBIDDEN, "U004", "활성 상태의 사용자만 가능합니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
