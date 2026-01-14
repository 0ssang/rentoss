package com.rentoss.user.domain.oauth;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class OAuth2UserInfoFactoryTest {

    @Nested
    @DisplayName("Provider별 생성 테스트")
    class CreateByProvider {

        @Test
        @DisplayName("google 입력 시 GoogleUserInfo 객체를 반환한다")
        void createGoogle() {
            // given
            Map<String, Object> attributes = Map.of("sub", "123", "email", "g@gmail.com", "name", "g-user", "picture", "img");

            // when
            OAuth2UserInfo result = OAuth2UserInfoFactory.getOAuth2UserInfo("google", attributes);

            // then
            assertThat(result).isInstanceOf(GoogleUserInfo.class);
            assertThat(result.getEmail()).isEqualTo("g@gmail.com");
        }

        @Test
        @DisplayName("kakao 입력 시 KakaoUserInfo 객체를 반환한다")
        void createKakao() {
            // given
            Map<String, Object> attributes = Map.of("sub", "123");

            // when
            OAuth2UserInfo result = OAuth2UserInfoFactory.getOAuth2UserInfo("kakao", attributes);

            // then
            assertThat(result).isInstanceOf(KakaoUserInfo.class);
        }

        @Test
        @DisplayName("naver 입력 시 NaverUserInfo를 반환한다")
        void createNaver() {
            // given
            Map<String, Object> attributes = Map.of("sub", "123");

            // when
            OAuth2UserInfo result = OAuth2UserInfoFactory.getOAuth2UserInfo("naver", attributes);

            // then
            assertThat(result).isInstanceOf(NaverUserInfo.class);
        }

        @Test
        @DisplayName("지원하지 않는 provider 입력 시 예외가 발생한다")
        void unsupportedProvider() {
            // given
            Map<String, Object> attributes = Map.of("sub", "123");

            // then
            assertThatThrownBy(() -> OAuth2UserInfoFactory.getOAuth2UserInfo("github", attributes))
                    .isInstanceOf(OAuth2AuthenticationException.class);
        }
    }

    @Nested
    @DisplayName("Naver response 파싱 테스트")
    class NaverParsing {

        @Test
        @DisplayName("Naver의 중첩된 response 구조를 정확하게 파싱한다")
        void parseNaverResponse() {
            // given
            Map<String, Object> response = Map.of(
                    "id", "naver-123",
                    "email", "naver@naver.com",
                    "nickname", "네이버 유저",
                    "profile_image", "naver.jpg"
            );

            Map<String, Object> attributes = Map.of(
                    "sub", "123",
                    "response", response
            );

            // when
            OAuth2UserInfo result = OAuth2UserInfoFactory.getOAuth2UserInfo("naver", attributes);

            // then
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(result).isInstanceOf(NaverUserInfo.class);
                softly.assertThat(result.getProviderId()).isEqualTo("123");
                softly.assertThat(result.getEmail()).isEqualTo("naver@naver.com");
                softly.assertThat(result.getNickname()).isEqualTo("네이버 유저");
                softly.assertThat(result.getProfileImageUrl()).isEqualTo("naver.jpg");
            });
        }
    }
}
