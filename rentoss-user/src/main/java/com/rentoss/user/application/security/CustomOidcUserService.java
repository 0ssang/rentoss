package com.rentoss.user.application.security;

import com.rentoss.user.domain.SocialProvider;
import com.rentoss.user.domain.User;
import com.rentoss.user.domain.UserRepository;
import com.rentoss.user.domain.oauth.GoogleUserInfo;
import com.rentoss.user.domain.oauth.KakaoUserInfo;
import com.rentoss.user.domain.oauth.NaverUserInfo;
import com.rentoss.user.domain.oauth.OAuth2UserInfo;
import com.rentoss.user.domain.principal.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOidcUserService extends OidcUserService {
    private final UserRepository userRepository;

    @Override
    @Transactional
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = super.loadUser(userRequest);
        log.info("OIDC Attributes: {}", oidcUser.getAttributes());

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2UserInfo userInfo = extractUserInfo(registrationId, oidcUser.getAttributes());

        User user = saveOrUpdate(userInfo);

        return new UserPrincipal(user, oidcUser);
    }

    private OAuth2UserInfo extractUserInfo(String registrationId, Map<String, Object> attributes) {
        return switch (registrationId) {
            case "google" -> new GoogleUserInfo(attributes);
            case "kakao" -> new KakaoUserInfo(attributes);
            case "naver" -> new NaverUserInfo(attributes);
            default -> throw new OAuth2AuthenticationException("지원하지 않는 provider 입니다: "+ registrationId);
        };
    }

    private User saveOrUpdate(OAuth2UserInfo userInfo) {
        SocialProvider provider = SocialProvider.valueOf(userInfo.getProvider().toUpperCase());

        return userRepository.findBySocialIdAndProvider(userInfo.getProviderId(), provider)
                .map(entity -> {
                    entity.updateProfile(userInfo.getNickname(), userInfo.getProfileImageUrl());
                    return entity;
                })
                .orElseGet(() -> {
                    User newUser = User.create(
                            userInfo.getProviderId(),
                            provider,
                            userInfo.getEmail(),
                            userInfo.getEmail(),
                            userInfo.getProfileImageUrl()
                    );
                    return userRepository.save(newUser);
                });
    }
}
