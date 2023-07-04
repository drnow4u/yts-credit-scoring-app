package com.yolt.creditscoring.configuration.security.admin;

import com.yolt.creditscoring.service.securitymodule.jwt.JwtCreationService;
import org.jose4j.jwt.JwtClaims;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;

/**
 * Since the http session is not being used in our application, which is the default for spring security oauth
 * when performing authorization in external service (like Github), we are providing custom AuthorizationRequestRepository
 * based on a cookie.
 * <p>
 * The cookie value store a JWT that is additionally encode with the use of
 * {@link JwtCreationService}
 */
@Component
public class HttpCookieOAuth2AuthorizationRequestRepository implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {
    public static final String OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME = "oauth2_auth_request";
    private static final int cookieExpireSeconds = 180;

    private final JwtCreationService jwtCreationService;
    private final boolean cookiesHttpsOnly;

    public HttpCookieOAuth2AuthorizationRequestRepository(JwtCreationService jwtCreationService, @Value("${credit-scoring.cookies.https:true}") boolean cookiesHttpsOnly) {
        this.jwtCreationService = jwtCreationService;
        this.cookiesHttpsOnly = cookiesHttpsOnly;
    }

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        return CookieUtils.getCookie(request, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME)
                .map(cookie -> decodeCookie(cookie.getValue()))
                .orElse(null);
    }

    @Override
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest, HttpServletRequest request, HttpServletResponse response) {
        if (authorizationRequest == null) {
            CookieUtils.deleteCookie(request, response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME);
            return;
        }

        String jwtCookie = jwtCreationService.createJWTCookie(authorizationRequest);

        Cookie cookie = new Cookie(OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME, jwtCookie);
        cookie.setPath("/");
        if (cookiesHttpsOnly) {
            cookie.setHttpOnly(true);
            cookie.setSecure(true);
        }
        cookie.setMaxAge(cookieExpireSeconds);
        response.addCookie(cookie);
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request) {
        return this.loadAuthorizationRequest(request);
    }

    public void removeAuthorizationRequestCookies(HttpServletRequest request, HttpServletResponse response) {
        CookieUtils.deleteCookie(request, response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME);
    }

    public OAuth2AuthorizationRequest decodeCookie(String encodedCookie) {
        try {
            JwtClaims jwtClaims = jwtCreationService.getJwtClaimsFromDecryptedJwtAndPerformValidation(encodedCookie);

            return OAuth2AuthorizationRequest.authorizationCode()
                    .authorizationUri(jwtClaims.getClaimValue("authorizationUri", String.class))
                    .clientId(jwtClaims.getClaimValue("clientId", String.class))
                    .redirectUri(jwtClaims.getClaimValue("redirectUri", String.class))
                    .authorizationRequestUri(jwtClaims.getClaimValue("authorizationRequestUri", String.class))
                    .scopes(new HashSet<>(jwtClaims.getClaimValue("scopes", ArrayList.class)))
                    .state(jwtClaims.getClaimValue("state", String.class))
                    .additionalParameters(jwtClaims.getClaimValue("additionalParameters", LinkedHashMap.class))
                    .attributes(jwtClaims.getClaimValue("attributes", LinkedHashMap.class))
                    .build();
        } catch (Exception e) {
            throw new InternalAuthenticationServiceException(e.getMessage(), e.getCause());
        }
    }
}
