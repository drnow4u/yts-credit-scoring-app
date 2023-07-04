package com.yolt.creditscoring.configuration.security.admin;

import com.yolt.creditscoring.service.securitymodule.jwt.JwtCreationService;
import org.jose4j.jwt.JwtClaims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationResponseType;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class HttpCookieOAuth2AuthorizationRequestRepositoryTest {

    @Mock
    private JwtCreationService jwtCreationService;

    private HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;

    @BeforeEach
    void setUp() {
        httpCookieOAuth2AuthorizationRequestRepository = new HttpCookieOAuth2AuthorizationRequestRepository(jwtCreationService, true);
    }

    @Test
    void shouldLoadAuthorizationRequestFromCookie() {
        // Given
        Map<String, Object> expectedMapAttribute = new LinkedHashMap<>();
        expectedMapAttribute.put("key", "value");
        JwtClaims claims = new JwtClaims();
        claims.setClaim("authorizationUri", "SOME_AUTHORIZATION_URI");
        claims.setClaim("authorizationGrantType", AuthorizationGrantType.AUTHORIZATION_CODE);
        claims.setClaim("responseType", OAuth2AuthorizationResponseType.CODE);
        claims.setClaim("clientId", "SOME_CLIENT_ID");
        claims.setClaim("redirectUri", "SOME_REDIRECT_URI");
        claims.setClaim("scopes", new ArrayList<>(List.of("SOME_SCOPE")));
        claims.setClaim("state", "SOME_STATE");
        claims.setClaim("additionalParameters", expectedMapAttribute);
        claims.setClaim("authorizationRequestUri", "SOME_AUTHORIZATION_REQUEST_URI");
        claims.setClaim("attributes", expectedMapAttribute);
        Cookie cookie = new Cookie("oauth2_auth_request", "DECODED_JWT");

        given(jwtCreationService.getJwtClaimsFromDecryptedJwtAndPerformValidation("DECODED_JWT")).willReturn(claims);
        HttpServletRequest request = mock(HttpServletRequest.class);
        given(request.getCookies()).willReturn(new Cookie[]{cookie});

        // When
        OAuth2AuthorizationRequest result = httpCookieOAuth2AuthorizationRequestRepository.loadAuthorizationRequest(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getClientId()).isEqualTo("SOME_CLIENT_ID");
        assertThat(result.getAuthorizationUri()).isEqualTo("SOME_AUTHORIZATION_URI");
        assertThat(result.getRedirectUri()).isEqualTo("SOME_REDIRECT_URI");
        assertThat(result.getAuthorizationRequestUri()).isEqualTo("SOME_AUTHORIZATION_REQUEST_URI");
        assertThat(result.getScopes()).containsOnly("SOME_SCOPE");
        assertThat(result.getState()).isEqualTo("SOME_STATE");
        assertThat(result.getAdditionalParameters()).isEqualTo(expectedMapAttribute);
        assertThat(result.getAttributes()).isEqualTo(expectedMapAttribute);
    }

    @Test
    void shouldReturnNullWhenCookieWillNotBeFoundDuringLoadAuthorizationRequest() {
        // Given
        HttpServletRequest request = mock(HttpServletRequest.class);
        given(request.getCookies()).willReturn(new Cookie[]{});

        // When
        OAuth2AuthorizationRequest result = httpCookieOAuth2AuthorizationRequestRepository.loadAuthorizationRequest(request);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void shouldAddCookieWhenSavingAuthorizationRequest() {
        // Given
        ArgumentCaptor<Cookie> cookieArgumentCaptor = ArgumentCaptor.forClass(Cookie.class);
        OAuth2AuthorizationRequest authorizationRequest = OAuth2AuthorizationRequest.authorizationCode()
                .clientId("SOME_CLIENT_ID")
                .authorizationUri("SOME_AUTHORIZATION_URI")
                .build();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        given(jwtCreationService.createJWTCookie(authorizationRequest)).willReturn("SOME_JWT_COOKIE");

        // When
        httpCookieOAuth2AuthorizationRequestRepository
                .saveAuthorizationRequest(authorizationRequest, request, response);

        // Then
        then(response).should().addCookie(cookieArgumentCaptor.capture());
        Cookie cookie = cookieArgumentCaptor.getValue();

        assertThat(cookie.getName()).isEqualTo("oauth2_auth_request");
        assertThat(cookie.getValue()).isEqualTo("SOME_JWT_COOKIE");
        assertThat(cookie.getPath()).isEqualTo("/");
        assertThat(cookie.getMaxAge()).isEqualTo(180);
        assertThat(cookie.isHttpOnly()).isTrue();
        assertThat(cookie.getSecure()).isTrue();
    }

    @Test
    void shouldDeleteCookieWhenOAuth2AuthorizationRequestIsNull() {
        // Given
        ArgumentCaptor<Cookie> cookieArgumentCaptor = ArgumentCaptor.forClass(Cookie.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        given(request.getCookies()).willReturn(new Cookie[]{new Cookie("oauth2_auth_request", "SOME_VALUE")});

        // When
        httpCookieOAuth2AuthorizationRequestRepository
                .saveAuthorizationRequest(null, request, response);

        // Then
        then(response).should().addCookie(cookieArgumentCaptor.capture());
        Cookie cookie = cookieArgumentCaptor.getValue();

        assertThat(cookie.getName()).isEqualTo("oauth2_auth_request");
        assertThat(cookie.getValue()).isEmpty();
        assertThat(cookie.getPath()).isEqualTo("/");
        assertThat(cookie.getMaxAge()).isZero();
    }

    @Test
    void shouldRemoveAuthorizationRequestCookies() {
        // Given
        ArgumentCaptor<Cookie> cookieArgumentCaptor = ArgumentCaptor.forClass(Cookie.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        given(request.getCookies()).willReturn(new Cookie[]{new Cookie("oauth2_auth_request", "SOME_VALUE")});

        // When
        httpCookieOAuth2AuthorizationRequestRepository
                .removeAuthorizationRequestCookies(request, response);

        // Then
        then(response).should().addCookie(cookieArgumentCaptor.capture());
        Cookie cookie = cookieArgumentCaptor.getValue();

        assertThat(cookie.getName()).isEqualTo("oauth2_auth_request");
        assertThat(cookie.getValue()).isEmpty();
        assertThat(cookie.getPath()).isEqualTo("/");
        assertThat(cookie.getMaxAge()).isZero();
    }
}
