package com.rentoss.user.application.security;

import com.rentoss.user.domain.SocialProvider;
import com.rentoss.user.domain.User;
import com.rentoss.user.domain.UserRepository;
import com.rentoss.user.domain.oauth.*;
import com.rentoss.user.domain.principal.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOidcUserService extends OidcUserService {

    private final UserRepository userRepository;
    private final RestClient restClient;

    @Override
    @Transactional
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        ClientRegistration clientRegistration = userRequest.getClientRegistration();
        String registrationId = clientRegistration.getRegistrationId();

        String userInfoUri = clientRegistration.getProviderDetails().getUserInfoEndpoint().getUri();
        if ("naver".equals(registrationId)) {
            return processNaverUser(userRequest, userInfoUri);
        }

        return processStandardUser(userRequest);
    }

    private OidcUser processStandardUser(OidcUserRequest userRequest) {
        OidcUser oidcUser = super.loadUser(userRequest);
        return processUserRegistration(
                userRequest.getClientRegistration().getRegistrationId(),
                oidcUser.getAttributes(),
                oidcUser
        );
    }

    private OidcUser processNaverUser(OidcUserRequest userRequest, String userInfoUri) {
        ClientRegistration clientRegistration = ClientRegistration.withClientRegistration(userRequest.getClientRegistration())
                .userInfoUri(null)
                .build();

        OidcUserRequest newRequest = new OidcUserRequest(
                clientRegistration,
                userRequest.getAccessToken(),
                userRequest.getIdToken(),
                userRequest.getAdditionalParameters()
        );

        OidcUser oidcUser = super.loadUser(newRequest);

        Map<String, Object> mergedAttributes = fetchNaverUserInfo(userRequest, oidcUser.getAttributes(), userInfoUri);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        return processUserRegistration(registrationId, mergedAttributes, oidcUser);
    }

    private OidcUser processUserRegistration(String registrationId, Map<String, Object> attributes, OidcUser oidcUser) {
        log.info(" OIDC Attributes for {}: {}", registrationId, attributes);
        OAuth2UserInfo userInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId, attributes);
        User user = saveOrUpdate(userInfo);
        return new UserPrincipal(user, oidcUser);
    }

    private Map<String, Object> fetchNaverUserInfo(OidcUserRequest userRequest, Map<String, Object> originalAttributes, String userInfoUri) {
        try {
            Map<String, Object> response = restClient.get()
                    .uri(userInfoUri)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + userRequest.getAccessToken().getTokenValue())
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {
                    });

            Map<String, Object> mergedAttributes = new HashMap<>(originalAttributes);
            if(response != null) {
                mergedAttributes.putAll(response);
            }
            return mergedAttributes;
        } catch (Exception e) {
            log.error("failed to fetch Naver UserInfo", e);
            throw new OAuth2AuthenticationException("Failed to get user info from Naver");
        }
    }

    private User saveOrUpdate(OAuth2UserInfo userInfo) {
        SocialProvider provider = SocialProvider.valueOf(userInfo.getProvider().toUpperCase());

        User user = userRepository.findBySocialIdAndProvider(userInfo.getProviderId(), provider)
                .map(entity -> {
                    entity.updateProfile(userInfo.getNickname(), userInfo.getProfileImageUrl());
                    return entity;
                })
                .orElse(User.create(
                        userInfo.getProviderId(),
                        provider,
                        userInfo.getEmail(),
                        userInfo.getNickname(),
                        userInfo.getProfileImageUrl()
                ));
        return userRepository.save(user);
    }
}