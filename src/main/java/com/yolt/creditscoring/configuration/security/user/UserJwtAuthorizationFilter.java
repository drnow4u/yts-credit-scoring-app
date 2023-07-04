package com.yolt.creditscoring.configuration.security.user;


import com.yolt.creditscoring.configuration.security.SecurityRoles;
import com.yolt.creditscoring.controller.exception.ControllerExceptionHandlers;
import com.yolt.creditscoring.controller.exception.ErrorResponseDTO;
import com.yolt.creditscoring.exception.UserNotFoundException;
import com.yolt.creditscoring.service.securitymodule.jwt.JwtCreationService;
import com.yolt.creditscoring.service.user.UserStorageService;
import com.yolt.creditscoring.service.user.model.CreditScoreUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHeaders;
import org.jose4j.jwt.JwtClaims;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

/**
 * JWT Filter used both for Credit Score user.
 * <p>
 * Validates if Authorization header is present and if the JWT is valid (after decrypting it).
 * If yes it creates the security context for the Credit Score user role.
 * If the JWT is not valid a 401 is returned.
 * <p>
 * If no Authorization is present the filter chain continues, without security context. It is used for open resources
 * for credit score user, that do not required authentication.
 */
@Slf4j
@RequiredArgsConstructor
public class UserJwtAuthorizationFilter extends OncePerRequestFilter {

    private static final String PREFIX = "Bearer ";

    private final JwtCreationService jwtCreationService;
    private final UserStorageService userStorageService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException {
        try {
            if (checkJWTTokenIsPresentInHeader(request)) {

                String encryptedJwtToken = request.getHeader(HttpHeaders.AUTHORIZATION);
                JwtClaims jwtClaims = jwtCreationService.getJwtClaimsFromDecryptedJwtAndPerformValidation(encryptedJwtToken.replace(PREFIX, ""));

                String userHash = jwtClaims.getSubject();

                CreditScoreUserPrincipal principal = userStorageService.findByInvitationHash(userHash)
                        .map(this::mapToCreditScoreUserPrincipal)
                        .orElseThrow(() -> new UserNotFoundException("Principal was not found with given hash"));

                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(principal, null,
                        Collections.singletonList(new SimpleGrantedAuthority(SecurityRoles.ROLE_PREFIX + SecurityRoles.CREDIT_SCORE_USER)));

                SecurityContextHolder.getContext().setAuthentication(auth);

            } else {
                SecurityContextHolder.clearContext();
            }

            filterChain.doFilter(request, response);
        } catch (Exception e) {
            SecurityContextHolder.clearContext();
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            ControllerExceptionHandlers.serializeErrorResponse(request, response, new ErrorResponseDTO(), e);
        }
    }

    private CreditScoreUserPrincipal mapToCreditScoreUserPrincipal(CreditScoreUser user) {
        return CreditScoreUserPrincipal.builder()
                .userId(user.getId())
                .clientId(user.getClientId())
                .yoltUserId(user.getYoltUserId())
                .yoltUserSiteId(user.getYoltUserSiteId())
                .yoltActivityId(user.getYoltActivityId())
                .initRequestInvitationStatus(user.getStatus())
                .build();
    }

    private boolean checkJWTTokenIsPresentInHeader(HttpServletRequest request) {
        String authenticationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        return authenticationHeader != null;
    }
}
