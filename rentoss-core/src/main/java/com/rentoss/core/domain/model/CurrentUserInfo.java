package com.rentoss.core.domain.model;

import com.rentoss.core.domain.enums.UserRole;

public record CurrentUserInfo(
        Long userId,
        String email,
        String nickname,
        String profileImageUrl,
        UserRole role
) {
    public static CurrentUserInfo of(Long userId, String email, String nickname, String profileImageUrl, UserRole role) {
        return new CurrentUserInfo(userId, email, nickname, profileImageUrl, role);
    }
}
