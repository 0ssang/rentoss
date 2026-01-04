package com.rentoss.user.domain;

import com.rentoss.core.domain.BaseEntity;
import com.rentoss.core.domain.model.Location;
import com.rentoss.core.common.exception.BusinessException;
import com.rentoss.user.exception.UserErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users", schema = "user_schema")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String socialId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SocialProvider provider;

    private String email;

    @Column(nullable = false)
    private String nickname;

    private String profileImageUrl;

    @Embedded
    private Location location;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;

    @Builder(access = AccessLevel.PRIVATE)
    private User(String socialId, SocialProvider provider, String email, String nickname, String profileImageUrl) {
        this.socialId = socialId;
        this.provider = provider;
        this.email = email;
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
        this.status = UserStatus.ACTIVE;
    }

    public static User create(String socialId, SocialProvider provider, String email, String nickname, String profileImageUrl) {
        return User.builder()
                .socialId(socialId)
                .provider(provider)
                .email(email)
                .nickname(nickname)
                .profileImageUrl(profileImageUrl)
                .build();
    }

    public void updateProfile(String nickname, String profileImageUrl) {
        validateActive();
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
    }

    public void updateLocation(Location location) {
        validateActive();
        this.location = location;
    }

    public void withdraw(){
        if (this.status == UserStatus.WITHDRAWN) {
            throw new BusinessException(UserErrorCode.ALREADY_WITHDRAWN);
        }
        this.status = UserStatus.WITHDRAWN;
    }

    private void validateActive() {
        if (this.status != UserStatus.ACTIVE) {
            throw new BusinessException(UserErrorCode.USER_NOT_ACTIVE);
        }
    }
}
