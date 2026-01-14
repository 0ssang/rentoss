package com.rentoss.user.domain.principal;

import com.rentoss.core.domain.enums.UserRole;
import com.rentoss.user.domain.SocialProvider;
import com.rentoss.user.domain.User;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collection;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class UserPrincipalTest {

    @Nested
    @DisplayName("생성 및 매핑 테스트")
    class Construction {

        @Test
        @DisplayName("UserPrincipal은 User 엔티티의 ID를 Name으로 반환하고 Role을 권한으로 변환한다")
        void createAndMap() {
            // given
            User user = User.create("social-id-123", SocialProvider.GOOGLE, "test@email.com", "tester", "img");
            ReflectionTestUtils.setField(user, "id", 100L);

            OidcUser mockOidcUser = mock(OidcUser.class);

            // when
            UserPrincipal principal = new UserPrincipal(user, mockOidcUser);

            // then
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(principal.getName()).isEqualTo("100");
                softly.assertThat(principal.getAuthorities())
                        .extracting(GrantedAuthority::getAuthority)
                        .containsExactly("ROLE_USER");
            });
        }
    }

    @Nested
    @DisplayName("OidcUser 위임 테스트")
    class Delegation {

        @Test
        @DisplayName("OIDC 관련 메서드 호출 시 내부 OidcUser 객체에게 위임한다")
        void delegateMethods() {
            // given
            User user = User.create("123", SocialProvider.GOOGLE, "test", "test", "img");
            OidcUser mockOidcUser = mock(OidcUser.class);

            Map<String, Object> attributes = Map.of("attr", "value");
            Map<String, Object> claims = Map.of("sub", "12345");
            OidcUserInfo userInfo = mock(OidcUserInfo.class);
            OidcIdToken idToken = mock(OidcIdToken.class);

            given(mockOidcUser.getAttributes()).willReturn(attributes);
            given(mockOidcUser.getClaims()).willReturn(claims);
            given(mockOidcUser.getUserInfo()).willReturn(userInfo);
            given(mockOidcUser.getIdToken()).willReturn(idToken);

            UserPrincipal principal = new UserPrincipal(user, mockOidcUser);

            // when & then
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(principal.getAttributes()).isEqualTo(attributes);
                softly.assertThat(principal.getClaims()).isEqualTo(claims);
                softly.assertThat(principal.getUserInfo()).isEqualTo(userInfo);
                softly.assertThat(principal.getIdToken()).isEqualTo(idToken);
            });
        }
    }
}
