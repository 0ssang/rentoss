package com.rentoss.user.presentation.dto.response;

import com.rentoss.user.domain.User;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record UserResponse(
        Long id,
        String nickname,
        String profileImageUrl,
        String address,
        LocalDateTime createdAt
) {
    public static UserResponse from(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .nickname(user.getNickname())
                .profileImageUrl(user.getProfileImageUrl())
                .address(user.getLocation() != null ? user.getLocation().getAddress() : null)
                .createdAt(user.getCreatedAt())
                .build();
    }
}
