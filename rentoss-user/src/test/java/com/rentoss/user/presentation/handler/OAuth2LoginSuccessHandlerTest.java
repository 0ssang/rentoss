package com.rentoss.user.presentation.handler;

import com.rentoss.core.domain.enums.UserRole;
import com.rentoss.core.security.cookie.CookieUtils;
import com.rentoss.core.security.jwt.JwtProvider;
import com.rentoss.user.domain.User;
import com.rentoss.user.domain.principal.UserPrincipal;
import com.rentoss.user.domain.token.RefreshToken;
import com.rentoss.user.domain.token.RefreshTokenRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class OAuth2LoginSuccessHandlerTest {

    @InjectMocks
    private OAuth2LoginSuccessHandler successHandler;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private CookieUtils cookieUtils;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Test
    @DisplayName("로그인 성공 시 JWT 발급, Redis 저장, 쿠키 설정 후 리다이렉트 한다")
    void onAuthenticationSuccess() throws Exception {
        // given
        User user = mock(User.class);
        given(user.getId()).willReturn(1L);
        given(user.getEmail()).willReturn("test@email.com");
        given(user.getNickname()).willReturn("tester");
        given(user.getProfileImageUrl()).willReturn("img.jpg");
        given(user.getRole()).willReturn(UserRole.USER);

        UserPrincipal principal = mock(UserPrincipal.class);
        given(principal.getUser()).willReturn(user);

        Authentication authentication = mock(Authentication.class);
        given(authentication.getPrincipal()).willReturn(principal);

        String accessToken = "access-token";
        String refreshToken = "refresh-token";
        given(jwtProvider.createAccessToken(1L, "test@email.com", "tester", "img.jpg", UserRole.USER))
                .willReturn(accessToken);
        given(jwtProvider.createRefreshToken(1L))
                .willReturn(refreshToken);

        ResponseCookie accessCookie = ResponseCookie.from("access-token", accessToken).path("/").build();
        ResponseCookie refreshCookie = ResponseCookie.from("refresh-token", refreshToken).path("/").build();
        given(cookieUtils.createAccessTokenCookie(accessToken)).willReturn(accessCookie);
        given(cookieUtils.createRefreshTokenCookie(refreshToken)).willReturn(refreshCookie);

        String redirectUri = "http://localhost:3000/callback";
        ReflectionTestUtils.setField(successHandler, "redirectUri", redirectUri);

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        // when
        successHandler.onAuthenticationSuccess(request, response, authentication);

        // then
        then(refreshTokenRepository).should().save(any(RefreshToken.class));

        assertThat(response.getHeaders(HttpHeaders.SET_COOKIE)).hasSize(2);
        assertThat(response.getHeader(HttpHeaders.SET_COOKIE)).contains("access-token");

        assertThat(response.getRedirectedUrl()).isEqualTo(redirectUri);
    }
}
