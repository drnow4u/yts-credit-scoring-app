package com.yolt.creditscoring.configuration.security.admin;

import com.yolt.creditscoring.service.clientadmin.model.AuthProvider;
import com.yolt.creditscoring.service.securitymodule.jwt.JwtCreationService;
import org.jose4j.jwt.JwtClaims;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OAuth2UserInfoFactoryTest {

    @Mock
    JwtCreationService jwtCreationService;

    @Test
    void shouldCreateGithubOAuthProvider() {
        // Given
        Map<String, Object> githubAttributes = Map.of(
                "id", 123456789,
                "email", "someEmail@test.com"
        );
        OAuth2UserRequest oAuth2UserRequest = new OAuth2UserRequest(createClientRegistration("GITHUB"),
                new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, "value", Instant.now(), Instant.now().plusSeconds(10)),
                githubAttributes);

        // When
        OAuth2UserInfo result = OAuth2UserInfoFactory.getOAuth2UserInfo(oAuth2UserRequest, githubAttributes, jwtCreationService);

        // Then
        assertThat(result.idpId()).isEqualTo("123456789");
        assertThat(result.oAuthProvider()).isEqualTo(AuthProvider.GITHUB);
    }

    @Test
    void shouldCreateGoogleOAuthProvider() {
        // Given
        Map<String, Object> githubAttributes = Map.of(
                "sub", "1912340958670298347",
                "email", "someEmail@gmail.com"
        );
        OAuth2UserRequest oAuth2UserRequest = new OAuth2UserRequest(createClientRegistration("GOOGLE"),
                new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, "value", Instant.now(), Instant.now().plusSeconds(10)),
                githubAttributes);

        // When
        OAuth2UserInfo result = OAuth2UserInfoFactory.getOAuth2UserInfo(oAuth2UserRequest, githubAttributes, jwtCreationService);

        // Then
        assertThat(result.idpId()).isEqualTo("1912340958670298347");
        assertThat(result.email()).isEqualTo("someEmail@gmail.com");
        assertThat(result.oAuthProvider()).isEqualTo(AuthProvider.GOOGLE);
    }

    @Test
    void shouldCreateMicrosoftOAuthProvider() {
        // Given
        Map<String, Object> githubAttributes = Map.of(
                "sub", "ZddLp4tCcqNxD1UmEPVECAZlYwvpPjBfbxwW5K0u",
                "email", "someEmail@outlook.com",
                "id_token", "idToken"
        );
        OAuth2UserRequest oAuth2UserRequest = new OAuth2UserRequest(
                createClientRegistration("MICROSOFT"),
                new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, "value", Instant.now(), Instant.now().plusSeconds(10)),
                githubAttributes);

        JwtClaims jwtClaims = new JwtClaims();
        jwtClaims.setClaim("oid", "test_user");
        jwtClaims.setClaim("tid", UUID.randomUUID().toString());
        when(jwtCreationService.getJwtClaims("idToken")).thenReturn(jwtClaims);

        // When
        OAuth2UserInfo result = OAuth2UserInfoFactory.getOAuth2UserInfo(oAuth2UserRequest, githubAttributes, jwtCreationService);

        // Then
        assertThat(result.idpId()).isEqualTo("test_user");
        assertThat(result.email()).isEqualTo("someEmail@outlook.com");
        assertThat(result.oAuthProvider()).isEqualTo(AuthProvider.MICROSOFT);
    }

    private ClientRegistration createClientRegistration(String registrationId) {
        return ClientRegistration.withRegistrationId(registrationId)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .clientId("clientId")
                .redirectUri("url")
                .authorizationUri("authurl")
                .tokenUri("tokenuri")
                .build();
    }
}