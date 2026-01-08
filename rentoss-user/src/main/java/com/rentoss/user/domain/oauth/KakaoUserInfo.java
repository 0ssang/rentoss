package com.rentoss.user.domain.oauth;

import java.util.Map;

public class KakaoUserInfo implements OAuth2UserInfo {

    private final Map<String, Object> attributes;

    public KakaoUserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String getProviderId() {
        return attributes.get("sub").toString();
    }

    @Override
    public String getProvider() {
        return "kakao";
    }

    @Override
    @SuppressWarnings("unchecked")
    public String getEmail() {
        if(attributes.containsKey("email")) {
            return (String) attributes.get("email");
        }

        Map<String, Object> account = (Map<String, Object>) attributes.get("kakao_account");
        if(account != null && account.containsKey("email")) {
            return (String) account.get("email");
        }

        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public String getNickname() {
        if(attributes.containsKey("nickname")) {
            return (String) attributes.get("nickname");
        }

        Map<String, Object> properties = (Map<String, Object>) attributes.get("properties");
        if(properties != null && properties.containsKey("nickname")) {
            return (String) properties.get("nickname");
        }

        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public String getProfileImageUrl() {
        if(attributes.containsKey("picture")) {
            return (String) attributes.get("picture");
        }

        Map<String, Object> properties = (Map<String, Object>) attributes.get("properties");
        if(properties != null && properties.containsKey("profile_image")) {
            return (String) properties.get("profile_image");
        }

        return null;
    }
}
