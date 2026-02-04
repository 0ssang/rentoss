package com.rentoss.core.security.resolver;

import com.rentoss.core.domain.enums.UserRole;
import com.rentoss.core.domain.model.CurrentUserInfo;
import com.rentoss.core.security.annotation.CurrentUser;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Slf4j
public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {

    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String USER_ROLE_HEADER = "X-User-Role";

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        // 1. 파라미터에 @CurrentUser 어놑테이션이 붙어있고
        // 2. 파라미터 타입이 CurrentUserInfo 클래스인 경우에만 동작
        return parameter.hasParameterAnnotation(CurrentUser.class)
                && parameter.getParameterType().equals(CurrentUserInfo.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavController, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();

        // 헤더에서 값 추출
        String userIdStr = request.getHeader(USER_ID_HEADER);
        String roleStr = request.getHeader(USER_ROLE_HEADER);

        // 헤더가 없으면 null 변환 (비로그인 사용자 처리 가능)
        if (!StringUtils.hasText(userIdStr)) {
            return null;
        }

        try {
            Long userId = Long.parseLong(userIdStr);
            // Role이 없으면 기본 값 USER, 있으면 Enum 반환
            UserRole role = StringUtils.hasText(roleStr) ? UserRole.valueOf(roleStr) : UserRole.USER;
            // Gateway에서 넘어온 ID와 Role만 채우고 나머지는 null
            return CurrentUserInfo.of(userId, null, null, null, role);
        } catch (Exception e) {
            log.error("Failed to resolve CurrentUser from headers. userId: {}, role: {}", userIdStr, roleStr, e);
            return null; // 파싱 에러 시 null 반환하여 안전하게 처리
        }
    }
}
