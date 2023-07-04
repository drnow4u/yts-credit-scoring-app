package com.yolt.creditscoring.configuration.security.admin;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
@RequiredArgsConstructor
public class CustomOidcUserService extends OidcUserService {

    private final CustomOAuth2UserService oauth2UserService;

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2AdminUser oauth2User = this.oauth2UserService.loadUser(userRequest);
        return new CfaOidcUser(oauth2User.getAuthorities(), userRequest.getIdToken(), oauth2User);
    }

    @Getter
    static class CfaOidcUser extends DefaultOidcUser {
        private final OAuth2AdminUser oAuth2User;

        public CfaOidcUser(Collection<? extends GrantedAuthority> authorities, OidcIdToken idToken, OAuth2AdminUser oAuth2User) {
            super(authorities, idToken);
            this.oAuth2User = oAuth2User;
        }
    }
}
