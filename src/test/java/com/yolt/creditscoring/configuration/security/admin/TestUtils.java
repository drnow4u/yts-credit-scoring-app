package com.yolt.creditscoring.configuration.security.admin;

import com.yolt.creditscoring.configuration.security.SecurityRoles;
import com.yolt.creditscoring.service.clientadmin.model.AuthProvider;
import com.yolt.creditscoring.service.clientadmin.model.ClientAdmin;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.yolt.creditscoring.TestUtils.*;

public class TestUtils {
    public static final OAuth2AdminUser OAUTH_ADMIN_USER_CLIENT_ADMIN = new OAuth2AdminUser(List.of(new SimpleGrantedAuthority(SecurityRoles.ROLE_PREFIX + SecurityRoles.CLIENT_ADMIN)),
            new OAuth2UserInfo(SOME_CLIENT_ADMIN_IDP_ID, SOME_CLIENT_ADMIN_EMAIL, AuthProvider.GOOGLE, Map.of(), null),
            new ClientAdmin(SOME_CLIENT_ADMIN_ID, SOME_CLIENT_ADMIN_EMAIL, SOME_CLIENT_ID, SOME_CLIENT_ADMIN_IDP_ID, AuthProvider.MICROSOFT));
    public static final OAuth2AdminUser OAUTH_ADMIN_USER_CLIENT_ADMIN_2 = new OAuth2AdminUser(List.of(new SimpleGrantedAuthority(SecurityRoles.ROLE_PREFIX + SecurityRoles.CLIENT_ADMIN)),
            new OAuth2UserInfo(SOME_CLIENT_2_ADMIN_IDP_ID, SOME_CLIENT_2_ADMIN_EMAIL, AuthProvider.GOOGLE, Map.of(), null),
            new ClientAdmin(SOME_CLIENT_2_ADMIN_ID, SOME_CLIENT_2_ADMIN_EMAIL, SOME_CLIENT_ID_2, SOME_CLIENT_2_ADMIN_IDP_ID, AuthProvider.MICROSOFT));

       public static final OAuth2AdminUser OAUTH_USER_CFA_ADMIN =  new OAuth2AdminUser(List.of(new SimpleGrantedAuthority(SecurityRoles.ROLE_PREFIX + SecurityRoles.CFA_ADMIN)),
            new OAuth2UserInfo(UUID.randomUUID().toString(),"cfaadmin@yolt.com", AuthProvider.GOOGLE, Map.of(), null),
                null);

    public static OAuth2AdminUser createOauth2AdminUser(ClientAdmin clientAdmin) {
        return new OAuth2AdminUser(List.of(new SimpleGrantedAuthority(SecurityRoles.ROLE_PREFIX + SecurityRoles.CLIENT_ADMIN)),
                new OAuth2UserInfo(clientAdmin.getIdpId(), clientAdmin.getEmail(), AuthProvider.GOOGLE, Map.of(), null),
                clientAdmin);
    };
}
