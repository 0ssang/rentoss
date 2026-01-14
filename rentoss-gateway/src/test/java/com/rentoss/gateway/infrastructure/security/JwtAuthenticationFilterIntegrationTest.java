package com.rentoss.gateway.infrastructure.security;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.rentoss.core.domain.enums.UserRole;
import com.rentoss.core.security.jwt.JwtProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration"
})
@AutoConfigureWireMock(port = 8089)
@ActiveProfiles("test")
class JwtAuthenticationFilterIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private JwtProvider jwtProvider;

    @Test
    @DisplayName("유효한 JWT 쿠키가 있으면 헤더에 유저 정보가 추가되어 전달된다")
    void shouldAddHeadersWhenJwtIsValid() {
        // given
        Long userId = 1L;
        UserRole role = UserRole.USER;
        String token = jwtProvider.createAccessToken(userId, "test@email.com", "tester", "img.jpg", role);

        stubFor(get(urlEqualTo("/test/me"))
                .withHeader("X-User-Id", equalTo(String.valueOf(userId)))
                .withHeader("X-User-Role", equalTo(role.name()))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("{\"message\": \"success\"}")));

        // when & then
        webTestClient.get()
                .uri("/test/me")
                .header(HttpHeaders.COOKIE, "access_token=" + token)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.message").isEqualTo("success");

        verify(getRequestedFor(urlEqualTo("/test/me"))
                .withHeader("X-User-Id", equalTo(String.valueOf(userId)))
                .withHeader("X-User-Role", equalTo(role.name())));
    }

    @Test
    @DisplayName("JWT 쿠키가 없으면 헤더 추가 없이 그대로 전달된다")
    void shouldPassWithoutHeadersWhenJwtIsMissing() {
        // given
        stubFor(get(urlEqualTo("/test/public"))
                .willReturn(aResponse()
                        .withStatus(200)));

        // when & then
        webTestClient.get()
                .uri("/test/public")
                .exchange()
                .expectStatus().isOk();

        // 헤더가 없는지 검증
        verify(getRequestedFor(urlEqualTo("/test/public"))
                .withoutHeader("X-User-Id")
                .withoutHeader("X-User-Role"));
    }
}
