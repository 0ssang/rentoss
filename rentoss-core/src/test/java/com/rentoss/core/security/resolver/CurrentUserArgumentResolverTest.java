package com.rentoss.core.security.resolver;

import com.rentoss.core.domain.enums.UserRole;
import com.rentoss.core.domain.model.CurrentUserInfo;
import jakarta.servlet.http.HttpServletRequest;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.web.context.request.NativeWebRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class CurrentUserArgumentResolverTest {

    @InjectMocks
    private CurrentUserArgumentResolver resolver;

    @Mock
    private NativeWebRequest webRequest;

    @Mock
    private HttpServletRequest httpServletRequest;

    @BeforeEach
    void setUp() {
        // NativeWebRequest에서 실제 HttpServletRequest를 꺼낼 수 있도록 설정
        given(webRequest.getNativeRequest()).willReturn(httpServletRequest);
    }

    @Test
    @DisplayName("헤더에 유효한 X-User-Id와 X-User-Role이 있으면 CurrentUserInfo 객체를 반환한다")
    void success() throws Exception{
        // given
        given(httpServletRequest.getHeader("X-User-Id")).willReturn("1");
        given(httpServletRequest.getHeader("X-User-Role")).willReturn("USER");

        // when
        CurrentUserInfo result = (CurrentUserInfo) resolver.resolveArgument(
                mock(MethodParameter.class), null, webRequest, null);

        // then
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(result).isNotNull();
            softly.assertThat(result.userId()).isEqualTo(1L);
            softly.assertThat(result.role()).isEqualTo(UserRole.USER);
        });
    }

    @Test
    @DisplayName("X-User-Id 헤더가 없으면 null을 반환한다")
    void shouldReturnNullWhenUserIdHeaderIsMissing() throws Exception{
        // given
        given(httpServletRequest.getHeader("X-User-Id")).willReturn(null);

        // when
        CurrentUserInfo result = (CurrentUserInfo) resolver.resolveArgument(
                mock(MethodParameter.class), null, webRequest, null);

        // then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("X-User-Id의 형식이 올바르지 않으면 null을 반환한다")
    void shouldReturnNullWhenUserIdHeaderIsInvalid() throws Exception{
        // given
        given(httpServletRequest.getHeader("X-User-Id")).willReturn("abc");

        // when
        CurrentUserInfo result = (CurrentUserInfo) resolver.resolveArgument(
                mock(MethodParameter.class), null, webRequest, null);

        // then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("X-User-Role이 없거나 유효하지 않아도 기본값 혹은 예외 처리되어 null을 반환한다")
    void shouldReturnNullWhenRoleHeaderIsInvalid() throws Exception{
        // given
        given(httpServletRequest.getHeader("X-User-Id")).willReturn("1");
        given(httpServletRequest.getHeader("X-User-Role")).willReturn("INVALID_ROLE");

        // when
        CurrentUserInfo result = (CurrentUserInfo) resolver.resolveArgument(
                mock(MethodParameter.class), null, webRequest, null);

        // then
        assertThat(result).isNull();
    }
}
