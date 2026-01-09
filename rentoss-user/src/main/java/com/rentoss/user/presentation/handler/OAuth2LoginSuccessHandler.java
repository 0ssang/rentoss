package com.rentoss.user.presentation.handler;

import com.rentoss.core.security.cookie.CookieUtils;
import com.rentoss.core.security.jwt.JwtProvider;
import com.rentoss.user.domain.User;
import com.rentoss.user.domain.principal.UserPrincipal;
import com.rentoss.user.domain.token.RefreshToken;
import com.rentoss.user.domain.token.RefreshTokenRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtProvider jwtProvider;
    private final CookieUtils cookieUtils;
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${app.oauth2.authorized-redirect-uri}")
    private String redirectUri;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        User user = principal.getUser();

        log.info("OIDC Login 성공: userId={}, email={}", user.getId(), user.getEmail());

        String accessToken = jwtProvider.createAccessToken(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getProfileImageUrl(),
                user.getRole()
        );
        String refreshToken = jwtProvider.createRefreshToken(user.getId());

        refreshTokenRepository.save(new RefreshToken(String.valueOf(user.getId()), refreshToken));

        ResponseCookie accessCookie = cookieUtils.createAccessTokenCookie(accessToken);
        ResponseCookie refreshTokenCookie = cookieUtils.createRefreshTokenCookie(refreshToken);

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());

        getRedirectStrategy().sendRedirect(request, response, redirectUri);
    }
}
