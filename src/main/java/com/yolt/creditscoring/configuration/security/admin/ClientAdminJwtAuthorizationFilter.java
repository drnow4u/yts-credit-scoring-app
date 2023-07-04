package com.yolt.creditscoring.configuration.security.admin;


import com.yolt.creditscoring.controller.exception.ControllerExceptionHandlers;
import com.yolt.creditscoring.controller.exception.ErrorResponseDTO;
import com.yolt.creditscoring.service.securitymodule.jwt.JwtCreationService;
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
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * JWT Filter used both for Client Admin user.
 * <p>
 * Validates if Authorization header is present and if the JWT is valid (after decrypting it).
 * If yes it creates the security context for Client Admin role.
 * If the JWT is not valid a 401 is returned.
 * <p>
 * If no Authorization is present the filter chain continues, without security context. It is used for open resources
 * for client admin user, that do not required authentication.
 */
@Slf4j
@RequiredArgsConstructor
public class ClientAdminJwtAuthorizationFilter extends OncePerRequestFilter {

    private static final String PREFIX = "Bearer ";

    private final JwtCreationService jwtCreationService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException {
        try {
            if (checkJWTTokenIsPresentInHeader(request)) {

                String encryptedJwtToken = request.getHeader(HttpHeaders.AUTHORIZATION);
                JwtClaims jwtClaims = jwtCreationService.getJwtClaimsFromDecryptedJwtAndPerformValidation(encryptedJwtToken.replace(PREFIX, ""));

                List<String> roles = (List<String>) jwtClaims.getClaimValue(AdminClaims.ROLES);

                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(jwtClaims, null,
                        roles != null ? roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toSet()) : Set.of());

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

    private boolean checkJWTTokenIsPresentInHeader(HttpServletRequest request) {
        String authenticationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        return authenticationHeader != null;
    }
}
