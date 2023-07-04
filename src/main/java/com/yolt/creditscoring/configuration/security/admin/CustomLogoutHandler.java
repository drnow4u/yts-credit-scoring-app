package com.yolt.creditscoring.configuration.security.admin;

import com.yolt.creditscoring.exception.LogoutException;
import com.yolt.creditscoring.service.securitymodule.jwt.JwtCreationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHeaders;
import org.jose4j.jwt.JwtClaims;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomLogoutHandler implements LogoutHandler {

    private static final String PREFIX = "Bearer ";

    private final JwtCreationService jwtCreationService;

    @Override
    public void logout(HttpServletRequest request,
                       HttpServletResponse response,
                       Authentication authentication) {
        try {
            String encryptedJwtToken = request.getHeader(HttpHeaders.AUTHORIZATION);
            JwtClaims jwtClaims = jwtCreationService.getJwtClaimsFromDecryptedJwt(encryptedJwtToken.replace(PREFIX, ""));
            log.info("User with JWT ID {} has logged out from the application", jwtClaims.getJwtId()); //NOSHERIFF JWT ID is not a vulnerable data - logging the ID was required https://yolt.atlassian.net/browse/YTSAPP-158
        } catch (Exception e) {
            throw new LogoutException("There was an error when user was logging out");
        }
    }
}
