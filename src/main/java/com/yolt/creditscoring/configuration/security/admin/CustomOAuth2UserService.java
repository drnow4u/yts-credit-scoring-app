package com.yolt.creditscoring.configuration.security.admin;

import com.yolt.creditscoring.configuration.security.SecurityRoles;
import com.yolt.creditscoring.exception.OAuth2NotRegisteredAdminException;
import com.yolt.creditscoring.service.clientadmin.model.AuthProvider;
import com.yolt.creditscoring.service.clientadmin.model.ClientAdmin;
import com.yolt.creditscoring.service.clientadmin.model.ClientAdminRepository;
import com.yolt.creditscoring.service.securitymodule.jwt.JwtCreationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * After successful IDP authentication this class checks if the user with the idpId provided by IDP matches
 * the idpId for client admin in the credit score database
 */
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    static final UUID YOLT_MICROSOFT_TENANT = UUID.fromString("21707a2b-2fc1-4196-a147-bdbd9f732618");
    static final UUID CFA_ADMIN_SECURITY_GROUP = UUID.fromString("16d851d1-cd24-4f80-9d01-6b14b8ef1852");

    private final ClientAdminRepository clientAdminRepository;
    private final JwtCreationService jwtCreationService;
    private final TestCfaAdminProperties testCfaAdminProperties;

    @Override
    public OAuth2AdminUser loadUser(OAuth2UserRequest oAuth2UserRequest) {
        OAuth2User oAuth2User = super.loadUser(oAuth2UserRequest);
        try {
            return processOAuth2User(oAuth2User, oAuth2UserRequest);
        } catch (AuthenticationException ex) {
            // Throwing an instance of AuthenticationException will trigger the OAuth2AuthenticationFailureHandler
            throw ex;
        } catch (Exception ex) {
            throw new InternalAuthenticationServiceException(ex.getMessage(), ex.getCause());
        }
    }

    OAuth2AdminUser processOAuth2User(OAuth2User oAuth2User, OAuth2UserRequest oAuth2UserRequest) {

        // First get to know the user we're dealing with.
        OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(oAuth2UserRequest, oAuth2User.getAttributes(), jwtCreationService);

        // Authenticate this user as a client-admin / cfa admin
        return switch(oAuth2UserInfo.oAuthProvider()) {
            case GITHUB, GOOGLE -> authenticateClientAdmin(oAuth2UserInfo);
            case MICROSOFT -> {
                // users in microsoft can be both a client-admin, but also cfa admins. (cfa admins can only come from the yolt tenant with the correct security group)
                List<SimpleGrantedAuthority> microsoftAuthorities = new ArrayList<>();

                if (oAuth2UserInfo.hasCfaAdminSecurityRoleInYoltTenant() || testCfaAdminProperties.getMicrosoftIds().contains(oAuth2UserInfo.idpId())) {
                    microsoftAuthorities.add(new SimpleGrantedAuthority(SecurityRoles.ROLE_PREFIX + SecurityRoles.CFA_ADMIN));
                }
                Optional<ClientAdmin> clientAdmin = clientAdminRepository.findByIdpIdAndAuthProvider(oAuth2UserInfo.idpId(), oAuth2UserInfo.oAuthProvider());
                if (clientAdmin.isPresent()) {
                    microsoftAuthorities.add(new SimpleGrantedAuthority(SecurityRoles.ROLE_PREFIX + SecurityRoles.CLIENT_ADMIN));
                }

                if (microsoftAuthorities.isEmpty()) {
                    throw new OAuth2NotRegisteredAdminException(oAuth2UserInfo.idpId(), AuthProvider.MICROSOFT.name());
                }
                yield new OAuth2AdminUser(microsoftAuthorities, oAuth2UserInfo, clientAdmin.orElse(null));
            }
         };

    }

    private OAuth2AdminUser authenticateClientAdmin(OAuth2UserInfo oAuth2UserInfo) {
        Optional<ClientAdmin> clientAdmin = clientAdminRepository.findByIdpIdAndAuthProvider(oAuth2UserInfo.idpId(), oAuth2UserInfo.oAuthProvider());
        if (clientAdmin.isEmpty()) {
            throw new OAuth2NotRegisteredAdminException(oAuth2UserInfo.idpId(), oAuth2UserInfo.oAuthProvider().name());
        }
        return new OAuth2AdminUser(List.of(new SimpleGrantedAuthority(SecurityRoles.ROLE_PREFIX + SecurityRoles.CLIENT_ADMIN)), oAuth2UserInfo, clientAdmin.get());
    }

}
