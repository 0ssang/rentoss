package com.rentoss.core.security.jwt;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    private String secret;
    private Duration accessTokenExpiration;
    private Duration refreshTokenExpiration;
    private String accessTokenCookieName;
    private String refreshTokenCookieName;
    private boolean cookieSecure;
}
