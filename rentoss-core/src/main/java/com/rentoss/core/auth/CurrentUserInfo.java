package com.rentoss.core.auth;

public record CurrentUserInfo(
        Long userId,
        String role
) {
    public static CurrentUserInfo of(Long userId, String role) {
        return new CurrentUserInfo(userId, role);
    }
}
