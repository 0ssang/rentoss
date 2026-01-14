package com.rentoss.user.domain.oauth;

public interface OAuth2UserInfo {
    String getProviderId();
    String getProvider();
    String getEmail();
    String getNickname();
    String getProfileImageUrl();
}
