package com.rentoss.core.security.cookie;

import com.rentoss.core.security.jwt.JwtProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class CookieUtils {

    private final JwtProperties jwtProperties;

    public ResponseCookie createAccessTokenCookie(String token) {
        return createCookie(
                jwtProperties.getAccessTokenCookieName(),
                token,
                jwtProperties.getAccessTokenExpiration()
        );
    }

    public ResponseCookie createRefreshTokenCookie(String token) {
        return createCookie(
                jwtProperties.getRefreshTokenCookieName(),
                token,
                jwtProperties.getRefreshTokenExpiration()
        );
    }

    private ResponseCookie createCookie(String name, String value, Duration maxAge) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(jwtProperties.isCookieSecure())
                .path("/")
                .maxAge(maxAge)
                .sameSite("Lax")
                .build();
    }

    public ResponseCookie deleteAccessTokenCookie() {
        return ResponseCookie.from(jwtProperties.getAccessTokenCookieName(), "")
                .maxAge(0)
                .path("/")
                .build();
    }

    public ResponseCookie deleteRefreshTokenCookie() {
        return ResponseCookie.from(jwtProperties.getRefreshTokenCookieName(), "")
                .maxAge(0)
                .path("/")
                .build();
    }
}
