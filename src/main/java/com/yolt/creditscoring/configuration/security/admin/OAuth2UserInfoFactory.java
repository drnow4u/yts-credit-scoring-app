package com.yolt.creditscoring.configuration.security.admin;

import com.yolt.creditscoring.exception.OAuth2AuthenticationProcessingException;
import com.yolt.creditscoring.service.securitymodule.jwt.JwtCreationService;
import org.jose4j.jwt.JwtClaims;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.yolt.creditscoring.service.clientadmin.model.AuthProvider.*;

class OAuth2UserInfoFactory {

    static OAuth2UserInfo getOAuth2UserInfo(OAuth2UserRequest oAuth2UserRequest, Map<String, Object> attributes, JwtCreationService jwtCreationService) {

        return switch (valueOf(oAuth2UserRequest.getClientRegistration().getRegistrationId().toUpperCase())) {
            case GOOGLE -> new OAuth2UserInfo((String) attributes.get("sub"),  (String) attributes.get("email"), GOOGLE, attributes, null);
            case GITHUB -> new OAuth2UserInfo(((Integer) attributes.get("id")).toString(), null, GITHUB, attributes, null);
            case MICROSOFT -> {
                // For Microsoft we want to match user by oid parameter that is present in the access token
                // The reason for this is that "oid" is constant value for user and "sub" idpId different per application
                // "oid" is added to user attributes, and it replaces the idpId value
                Object idToken = oAuth2UserRequest.getAdditionalParameters().get("id_token");
                if (idToken == null) {
                    throw new OAuth2AuthenticationProcessingException("The id token is missing for Microsoft authentication");
                }

                JwtClaims jwtClaims = jwtCreationService.getJwtClaims(idToken.toString());
                String oid = (String) jwtClaims.getClaimValue("oid");
                if (oid == null) {
                    throw new OAuth2AuthenticationProcessingException("ID parameter not present in oauth user for provider: " + MICROSOFT);
                }
                MicrosoftInfo microsoftInfo = new MicrosoftInfo(UUID.fromString((String) jwtClaims.getClaimValue("tid")), (List<String>) jwtClaims.getClaimValue("groups"));
                yield new OAuth2UserInfo(oid, (String) attributes.get("email"), MICROSOFT, attributes, microsoftInfo);
            }
        };
    }
}