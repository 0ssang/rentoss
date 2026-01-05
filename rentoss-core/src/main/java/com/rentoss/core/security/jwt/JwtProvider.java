package com.rentoss.core.security.jwt;

import com.rentoss.core.common.exception.AuthErrorCode;
import com.rentoss.core.common.exception.BusinessException;
import com.rentoss.core.domain.enums.UserRole;
import com.rentoss.core.domain.model.CurrentUserInfo;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;


@Slf4j
@Component
public class JwtProvider {

    private final JwtProperties jwtProperties;
    private final SecretKey key;

    public JwtProvider(JwtProperties jwtProperties) {
        byte[] keybyte = Decoders.BASE64.decode(jwtProperties.getSecret());
        this.jwtProperties = jwtProperties;
        this.key = Keys.hmacShaKeyFor(keybyte);
    }

    // 토큰 생성
    public String createAccessToken(Long userId, String email, String nickname, String profileImageUrl, UserRole role) {
        return createToken(userId, email, nickname, profileImageUrl, role, jwtProperties.getAccessTokenExpiration().toMillis());
    }

    public String createRefreshToken(Long userId) {
        return createToken(userId, null, null, null, null, jwtProperties.getRefreshTokenExpiration().toMillis());
    }

    private String createToken(Long userId, String email, String nickname, String profileImageUrl, UserRole role, Long expireTime) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expireTime);

        JwtBuilder builder = Jwts.builder()
                .setSubject(String.valueOf(userId))
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(key, SignatureAlgorithm.HS256);

        if(role != null) {
            builder.claim("role", role.name());
            builder.claim("email", email);
            builder.claim("nickname", nickname);
            builder.claim("profileImageUrl", profileImageUrl);
        }

        return builder.compact();
    }

    // 토큰 검증
    public boolean validateToken(String token) {
        try {
            getClaims(token);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            log.warn("잘못된 JWT 서명입니다: {}", e.getMessage());
            throw new BusinessException(AuthErrorCode.INVALID_TOKEN);
        } catch (ExpiredJwtException e) {
            log.warn("만료된 JWT 토큰입니다: {}", e.getMessage());
            throw new BusinessException(AuthErrorCode.EXPIRED_TOKEN);
        } catch (UnsupportedJwtException e) {
            log.warn("지원되지 않는 JWT 토큰입니다: {}", e.getMessage());
            throw new BusinessException(AuthErrorCode.UNSUPPORTED_TOKEN);
        } catch (IllegalArgumentException e) {
            log.warn("JWT 토큰이 잘못되었습니다: {}", e.getMessage());
            throw new BusinessException(AuthErrorCode.TOKEN_NOT_FOUND);
        }
    }

    public Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // 정보 추출
    public CurrentUserInfo extractUserInfo(String token) {
        Claims claims = getClaims(token);

        Long userId = Long.valueOf(claims.getSubject());
        String email = claims.get("email", String.class);
        String nickname = claims.get("nickname", String.class);
        String profileImageUrl = claims.get("profileImageUrl", String.class);

        String roleStr = claims.get("role", String.class);
        UserRole role = roleStr != null ? UserRole.valueOf(roleStr) : null;

        return CurrentUserInfo.of(userId, email, nickname, profileImageUrl, role);
    }
}
