package com.rentoss.gateway.infrastructure.security;

import com.rentoss.core.domain.model.CurrentUserInfo;
import com.rentoss.core.security.jwt.JwtProperties;
import com.rentoss.core.security.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private final JwtProvider jwtProvider;
    private final JwtProperties jwtProperties;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    private static final List<String> WHITE_LIST = List.of(
            "/api/v1/auth/**",
            "/login/oauth2/**",
            "/oauth2/**",
            "/api/v1/items",
            "/api/v1/items/nearby",
            "/api/v1/items/search"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        if (isWhiteList(path)) {
            return chain.filter(exchange);
        }

        String token = extractToken(exchange);

        if (token != null && jwtProvider.validateToken(token)) {
            CurrentUserInfo userInfo = jwtProvider.extractUserInfo(token);

            ServerHttpRequest request = exchange.getRequest().mutate()
                    .header("X-User-Id", String.valueOf(userInfo.userId()))
                    .header("X-User-Role", userInfo.role().name())
                    .build();

            return chain.filter(exchange.mutate().request(request).build());
        }

        return chain.filter(exchange);
    }

    private boolean isWhiteList(String path) {
        return WHITE_LIST.stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    private String extractToken(ServerWebExchange exchange) {
        HttpCookie cookie = exchange.getRequest().getCookies().getFirst(jwtProperties.getAccessTokenCookieName());
        return (cookie != null) ? cookie.getValue() : null;
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
