package com.rentoss.user.domain.oauth;

import java.util.Map;

public class NaverUserInfo implements OAuth2UserInfo {

    private final Map<String, Object> attributes;
    private final String sub;

    @SuppressWarnings("unchecked")
    public NaverUserInfo(Map<String, Object> attributes) {
        this.sub = (String) attributes.get("sub");
        if (attributes.containsKey("response")) {
            this.attributes = (Map<String, Object>) attributes.get("response");
        } else {
            this.attributes = attributes;
        }
    }

    @Override
    public String getProviderId() {
        if (sub != null) {
            return sub;
        }
        return (String) attributes.get("id");
    }

    @Override
    public String getProvider() {
        return "naver";
    }

    @Override
    public String getEmail() {
        return (String) attributes.get("email");
    }

    @Override
    public String getNickname() {
        if (attributes.containsKey("nickname")) {
            return (String) attributes.get("nickname");
        }
        return (String) attributes.get("name");
    }

    @Override
    public String getProfileImageUrl() {
        return (String) attributes.get("profile_image");
    }
}
