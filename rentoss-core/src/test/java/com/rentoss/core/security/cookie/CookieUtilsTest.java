package com.rentoss.core.security.cookie;

import com.rentoss.core.security.jwt.JwtProperties;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseCookie;

import java.time.Duration;

public class CookieUtilsTest {

    private CookieUtils cookieUtils;
    private JwtProperties jwtProperties;

    @BeforeEach
    void setUp() {
        jwtProperties = new JwtProperties();
        jwtProperties.setAccessTokenCookieName("access_token");
        jwtProperties.setRefreshTokenCookieName("refresh_token");
        jwtProperties.setAccessTokenExpiration(Duration.ofMinutes(30));
        jwtProperties.setRefreshTokenExpiration(Duration.ofDays(7));
        jwtProperties.setCookieSecure(false);

        cookieUtils = new CookieUtils(jwtProperties);
    }

    @Nested
    @DisplayName("쿠키 생성 테스트")
    class CreateCookie {

        @Test
        @DisplayName("AccessToken 쿠키 생성 시 설정 값이 올바르게 적용된다")
        void createAccessTokenCookie() {
            // given
            String token = "faketokenfaketoken";

            // when
            ResponseCookie result = cookieUtils.createAccessTokenCookie(token);

            // then
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(result.getName()).isEqualTo("access_token");
                softly.assertThat(result.getMaxAge()).isEqualTo(Duration.ofMinutes(30));
                softly.assertThat(result.getPath()).isEqualTo("/");
                softly.assertThat(result.getValue()).isEqualTo(token);
                softly.assertThat(result.isSecure()).isFalse();
                softly.assertThat(result.isHttpOnly()).isTrue();
                softly.assertThat(result.getSameSite()).isEqualTo("Lax");
            });
        }

        @Test
        @DisplayName("RefreshToken 쿠키 생성 시 설정 값이 올바르게 적용된다")
        void createRefreshTokenCookie() {
            // given
            String token = "faketokenfaketoken";

            // when
            ResponseCookie result = cookieUtils.createRefreshTokenCookie(token);

            // then
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(result.getName()).isEqualTo("refresh_token");
                softly.assertThat(result.getMaxAge()).isEqualTo(Duration.ofDays(7));
                softly.assertThat(result.getPath()).isEqualTo("/");
                softly.assertThat(result.getValue()).isEqualTo(token);
                softly.assertThat(result.isSecure()).isFalse();
                softly.assertThat(result.isHttpOnly()).isTrue();
                softly.assertThat(result.getSameSite()).isEqualTo("Lax");
            });
        }
    }

    @Nested
    @DisplayName("쿠키 삭제 테스트")
    class DeleteCookie {

        @Test
        @DisplayName("AccessToken 삭제 쿠키는 Max-age가 0이고 값이 비어있다")
        void deleteAccessTokenCookie(){
            // when
            ResponseCookie result = cookieUtils.deleteAccessTokenCookie();

            // then
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(result.getName()).isEqualTo("access_token");
                softly.assertThat(result.getMaxAge()).isEqualTo(Duration.ZERO);
                softly.assertThat(result.getPath()).isEqualTo("/");
                softly.assertThat(result.getValue()).isEmpty();
            });
        }

        @Test
        @DisplayName("RefreshToken 삭제 쿠키는 Max-Age가 0이고 값이 비어있다")
        void deleteRefreshTokenCookie(){
            // when
            ResponseCookie result = cookieUtils.deleteRefreshTokenCookie();

            // then
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(result.getName()).isEqualTo("refresh_token");
                softly.assertThat(result.getMaxAge()).isEqualTo(Duration.ZERO);
                softly.assertThat(result.getPath()).isEqualTo("/");
                softly.assertThat(result.getValue()).isEmpty();
            });
        }
    }
}
