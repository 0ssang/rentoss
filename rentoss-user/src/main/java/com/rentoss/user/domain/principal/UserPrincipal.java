package com.rentoss.user.domain.principal;

import com.rentoss.user.domain.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

@Getter
@Slf4j
@RequiredArgsConstructor
public class UserPrincipal implements OidcUser {

    private final User user;
    private final OidcUser oidcUser;

    @Override
    public Map<String, Object> getAttributes() {
        log.info("getAttributes {}", oidcUser.getAttributes());
        return oidcUser.getAttributes();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Set.of(new SimpleGrantedAuthority(user.getRole().getKey()));
    }

    @Override
    public String getName() {
        return String.valueOf(user.getId());
    }

    @Override
    public Map<String, Object> getClaims() {
        return oidcUser.getClaims();
    }

    @Override
    public OidcUserInfo getUserInfo() {
        return oidcUser.getUserInfo();
    }

    @Override
    public OidcIdToken getIdToken() {
        return oidcUser.getIdToken();
    }
}
