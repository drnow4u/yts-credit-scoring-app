package com.yolt.creditscoring.configuration.security.customer;


import com.yolt.creditscoring.configuration.ClockConfig;
import com.yolt.creditscoring.configuration.security.SecurityRoles;
import com.yolt.creditscoring.controller.exception.ControllerExceptionHandlers;
import com.yolt.creditscoring.controller.exception.ErrorResponseDTO;
import com.yolt.creditscoring.controller.exception.ErrorType;
import com.yolt.creditscoring.exception.ClientTokenException;
import com.yolt.creditscoring.service.clienttoken.model.ClientTokenEntity;
import com.yolt.creditscoring.service.clienttoken.model.ClientTokenRepository;
import com.yolt.creditscoring.service.clienttoken.model.ClientTokenStatus;
import com.yolt.creditscoring.service.securitymodule.jwt.JwtCreationService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * JWT Filter used both for Client Token API.
 * <p>
 * Validates if Authorization header is present and if the JWT is valid (after decrypting it).
 * Checks if the token has permission for given endpoint.
 * If yes it creates the security context for Client Token role.
 * If the JWT is not valid a 401 is returned.
 */
@Slf4j
@RequiredArgsConstructor
public class ClientTokenJwtAuthorizationFilter extends OncePerRequestFilter {

    private static final Clock clock = ClockConfig.getClock();

    private static final String PREFIX = "(?i)Bearer ";

    private final JwtCreationService jwtCreationService;
    private final ClientTokenRepository clientTokenRepository;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws IOException {
        try {
            if (!checkJWTTokenIsPresentInHeader(request)) {
                throw new ClientTokenException("Missing Authorization header for client token");
            }

            String encryptedJwtToken = request.getHeader(HttpHeaders.AUTHORIZATION);

            UUID jwtId = jwtCreationService.getJwtIdFromDecryptedJwtWithoutValidation(encryptedJwtToken.replaceAll(PREFIX, ""));

            Optional<ClientTokenEntity> clientTokenEntityOptional = clientTokenRepository.findById(jwtId);
            if (clientTokenEntityOptional.isEmpty()) {
                throw new ClientTokenException("Could not find client token with id " + jwtId);
            }
            ClientTokenEntity clientTokenEntity = clientTokenEntityOptional.get();

            jwtCreationService.validateDecryptedJwt(
                    encryptedJwtToken.replaceAll(PREFIX, ""), clientTokenEntity.getSignedPublicKeyId());

            if (clientTokenEntity.getStatus() != ClientTokenStatus.ACTIVE) {
                throw new ClientTokenException("Client Token is not active");
            }

            clientTokenEntity.setLastAccessedDate(OffsetDateTime.now(clock));
            clientTokenRepository.save(clientTokenEntity);

            List<SimpleGrantedAuthority> simpleGrantedAuthorities = clientTokenEntity.getPermissions().stream()
                    .map(permission -> new SimpleGrantedAuthority(permission.getPermissionName())).collect(Collectors.toCollection(ArrayList::new));
            simpleGrantedAuthorities.add(new SimpleGrantedAuthority(SecurityRoles.ROLE_PREFIX + SecurityRoles.CLIENT_TOKEN));
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(mapClientTokenToClientTokenPrincipal(clientTokenEntity), null,
                    simpleGrantedAuthorities);

            SecurityContextHolder.getContext().setAuthentication(auth);
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            SecurityContextHolder.clearContext();
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            ControllerExceptionHandlers.serializeErrorResponse(request, response, new ErrorResponseDTO(ErrorType.TOKEN_INVALID), e);
        }
    }

    private ClientTokenPrincipal mapClientTokenToClientTokenPrincipal(ClientTokenEntity clientToken) {
        return ClientTokenPrincipal.builder()
                .tokenId(clientToken.getJwtId())
                .clientId(clientToken.getClientId())
                .email(clientToken.getCreatedAdminEmail())
                .build();
    }

    private boolean checkJWTTokenIsPresentInHeader(HttpServletRequest request) {
        String authenticationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        return authenticationHeader != null;
    }
}
