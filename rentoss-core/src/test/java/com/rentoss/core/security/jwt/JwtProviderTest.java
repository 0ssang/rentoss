package com.rentoss.core.security.jwt;

import com.rentoss.core.common.exception.AuthErrorCode;
import com.rentoss.core.common.exception.BusinessException;
import com.rentoss.core.domain.enums.UserRole;
import com.rentoss.core.domain.model.CurrentUserInfo;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.io.Encoders;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        void createAccessToken() {
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

    @Nested
    @DisplayName("토큰 검증 테스트")
    class VaildateToken {

        @Test
        @DisplayName("유효한 토큰은 검증에 성공한다")
        void success() {
            // given
            String token = jwtProvider.createAccessToken(1L, "test@test.com", "nick", "img", UserRole.USER);

            // when
            boolean isValid = jwtProvider.validateToken(token);

            // then
            assertThat(isValid).isTrue();
        }

        @Test
        @DisplayName("만료된 토큰은 EXPIRED_TOKEN 예외가 발생한다")
        void exprired() {
            // given
            JwtProperties shortProp = new JwtProperties();
            shortProp.setSecret(TEST_SECRET);
            shortProp.setAccessTokenExpiration(Duration.ofMillis(1));
            shortProp.setRefreshTokenExpiration(Duration.ofMillis(1));
            JwtProvider shortProvider = new JwtProvider(shortProp);

            String token = shortProvider.createAccessToken(1L, "test@test.com", "nick", "img", UserRole.USER);

            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {}

            // when & then
            assertThatThrownBy(() -> jwtProvider.validateToken(token))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(AuthErrorCode.EXPIRED_TOKEN);
        }

        @Test
        @DisplayName("서명이 다르거나 조작된 토큰은 INVALID_TOKEN 예외가 발생한다")
        void invalidSignature() {
            // given
            String fakeSecret = "OtherSecretKeyOtherSecretKeyOtherSecretKeyOtherSecretKeyOtherSecretKey";
            JwtProperties fakeProp = new JwtProperties();
            fakeProp.setSecret(Encoders.BASE64.encode(fakeSecret.getBytes()));
            fakeProp.setAccessTokenExpiration(Duration.ofMinutes(10));
            JwtProvider fakeProvider = new JwtProvider(fakeProp);

            String token = fakeProvider.createAccessToken(1L, "test@test.com", "nick", "img", UserRole.USER);

            // when & then
            assertThatThrownBy(() -> jwtProvider.validateToken(token))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(AuthErrorCode.INVALID_TOKEN);
        }

        @Test
        @DisplayName("토큰이 비어있거나 null이면 TOKEN_NOT_FOUND 예외가 발생한다")
        void emptyOrNull() {
            // given
            String token = "";

            // when & then
            assertThatThrownBy(() -> jwtProvider.validateToken(token))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(AuthErrorCode.TOKEN_NOT_FOUND);

            assertThatThrownBy(() -> jwtProvider.validateToken(null))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(AuthErrorCode.TOKEN_NOT_FOUND);
        }

        @Test
        @DisplayName("지원하지 않는 형식의 토큰은 UNSUPPORTED_TOKEN 예외가 발생한다")
        void unsupported() {
            // given
            // 헤더에 "alg": "none"을 넣어서 서명 없이 만듦
            String unsignedToken = Jwts.builder()
                    .setHeaderParam("alg", "none")
                    .setSubject("1")
                    .compact();

            // when & then
            assertThatThrownBy(() -> jwtProvider.validateToken(unsignedToken))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(AuthErrorCode.UNSUPPORTED_TOKEN);
        }
    }
}
