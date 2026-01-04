package com.rentoss.core.security.jwt;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    private String secret;
    private Long accessTokenExpiration;
    private String accessTokenCookieName;

    private Long refreshTokenExpiration;
    private String refreshTokenCookieName;
}
