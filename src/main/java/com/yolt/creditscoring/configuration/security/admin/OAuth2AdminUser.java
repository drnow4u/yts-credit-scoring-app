package com.yolt.creditscoring.configuration.security.admin;

import com.yolt.creditscoring.exception.OAuth2EmailMismatchException;
import com.yolt.creditscoring.service.clientadmin.model.ClientAdmin;
import lombok.Getter;
import org.springframework.lang.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;

import java.util.Collection;
import java.util.Optional;

/**
 * This class is a little bit lame and is only the authenticated principal during the oauth-flow. This is required because the
 * {@link CustomOAuth2UserService} needs to return a {@link OAuth2AuthenticatedPrincipal}.
 * <p>
 * In our {@link OAuth2AuthenticationSuccessHandler} a token is created for this principal. Then this token is used to call
 * the actual API. The token / JWTClaims -principal will then be converted/upcasted to either a {@link ClientAdminPrincipal} or {@link CfaAdminPrincipal}
 */
public class OAuth2AdminUser extends DefaultOAuth2User {

    private static final String NAME_ATTRIBUTE_KEY = "idpId";
    private final OAuth2UserInfo oAuth2UserInfo;
    private final ClientAdmin clientAdmin;
    @Getter
    private final String email;

    public OAuth2AdminUser(Collection<? extends GrantedAuthority> authorities, OAuth2UserInfo oAuth2UserInfo, @Nullable ClientAdmin clientAdmin) {
        super(authorities, oAuth2UserInfo.getAllUserAttributes(), NAME_ATTRIBUTE_KEY);
        this.oAuth2UserInfo = oAuth2UserInfo;
        this.clientAdmin = clientAdmin;
        email = validateEmail(oAuth2UserInfo, clientAdmin);
    }

    public String getIdpId() {
        return oAuth2UserInfo.idpId();
    }

    /**
     * Only returns when the logged in user is also a client-admin.
     */
    public Optional<ClientAdmin> getClientAdmin() {
        return Optional.ofNullable(clientAdmin);
    }

    private String validateEmail(OAuth2UserInfo oAuth2UserInfo, @Nullable ClientAdmin clientAdmin) {
        return switch (oAuth2UserInfo.oAuthProvider()) {
            case GITHUB -> {
                // No validation. We don't get the email from github. However, we don't have CFA Admins for github.
                // There is always a client-admin.
                if (clientAdmin == null) {
                    throw new IllegalStateException("No client admin for provider github. This is an invalid state.");
                }
                yield clientAdmin.getEmail();
            }
            case GOOGLE, MICROSOFT -> {
                // If there is a client admin, make sure the email is the same as what we get from google/microsoft.
                if (clientAdmin != null && !clientAdmin.getEmail().equalsIgnoreCase(oAuth2UserInfo.email())) {
                    throw OAuth2EmailMismatchException.builder()
                            .clientID(clientAdmin.getClientId())
                            .idpId(oAuth2UserInfo.idpId())
                            .storedClientAdminEmail(clientAdmin.getEmail())
                            .providedClientAdminEmail(oAuth2UserInfo.email())
                            .provider(oAuth2UserInfo.oAuthProvider().name())
                            .build();
                }
                yield oAuth2UserInfo.email();
            }
        };
    }

}
