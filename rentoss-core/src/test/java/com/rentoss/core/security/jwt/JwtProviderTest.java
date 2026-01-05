package com.rentoss.core.security.jwt;

import com.rentoss.core.domain.enums.UserRole;
import com.rentoss.core.domain.model.CurrentUserInfo;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;

public class JwtProviderTest {

    private JwtProvider jwtProvider;
    private JwtProperties jwtProperties;

    private static final String TEST_SECRET = "c2lsdmVybmluZS10ZWNoLXNwcmluZy1ib290LWp3dC10dXRvcmlhbC1zZWNyZXQtc2lsdmVybmluZS10ZWNoLXNwcmluZy1ib290LWp3dC10dXRvcmlhbC1zZWNyZXQK";

    @BeforeEach
    public void setUp() {
        jwtProperties = new JwtProperties();
        jwtProperties.setSecret(TEST_SECRET);
        jwtProperties.setAccessTokenExpiration(Duration.ofMinutes(30));
        jwtProperties.setRefreshTokenExpiration(Duration.ofDays(7));

        jwtProvider = new JwtProvider(jwtProperties);
    }

    @Nested
    @DisplayName("토큰 생성 테스트")
    class CreateToken {

        @Test
        @DisplayName("Access Token 생성 성공한다")
        void createToken() {
            // given
            Long userId = 1L;
            String email = "test@rentoss.com";
            String nickname = "test_user";
            String profileImageUrl = "https://rentoss.com/img/profile.jpg";
            UserRole role = UserRole.USER;

            // when
            String token = jwtProvider.createAccessToken(userId, email, nickname, profileImageUrl, role);
            CurrentUserInfo result = jwtProvider.extractUserInfo(token);

            // then
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(result.userId()).isEqualTo(userId);
                softly.assertThat(result.email()).isEqualTo(email);
                softly.assertThat(result.nickname()).isEqualTo(nickname);
                softly.assertThat(result.profileImageUrl()).isEqualTo(profileImageUrl);
                softly.assertThat(result.role()).isEqualTo(role);
            });
        }

        @Test
        @DisplayName("RefreshToken 생성에 성공한다")
        void createRefreshToken() {
            // given
            Long userId = 1L;

            // when
            String token = jwtProvider.createRefreshToken(userId);
            CurrentUserInfo result = jwtProvider.extractUserInfo(token);

            // then
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(result.userId()).isEqualTo(userId);
                softly.assertThat(result.role()).isNull();
                softly.assertThat(result.email()).isNull();
                softly.assertThat(result.nickname()).isNull();
                softly.assertThat(result.profileImageUrl()).isNull();
            });
        }
    }
}
