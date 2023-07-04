package com.yolt.creditscoring.configuration.security.customer;

import com.yolt.creditscoring.configuration.security.SecurityRoles;
import com.yolt.creditscoring.service.clienttoken.model.ClientTokenEntity;
import com.yolt.creditscoring.service.clienttoken.model.ClientTokenPermission;
import com.yolt.creditscoring.service.clienttoken.model.ClientTokenRepository;
import com.yolt.creditscoring.service.clienttoken.model.ClientTokenStatus;
import com.yolt.creditscoring.service.securitymodule.jwt.JwtCreationService;
import com.yolt.creditscoring.service.securitymodule.semaevent.SemaEventService;
import org.apache.http.HttpHeaders;
import org.jose4j.jwt.JwtClaims;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.FilterChain;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static com.yolt.creditscoring.TestUtils.*;
import static com.yolt.creditscoring.controller.customer.CustomerAPIController.INVITE_USER_CLIENT_TOKEN_ENDPOINT;
import static com.yolt.creditscoring.service.clienttoken.model.ClientTokenPermission.INVITE_USER;
import static com.yolt.creditscoring.service.securitymodule.jwt.JwtCreationService.CLIENT_TOKEN_SUBJECT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class ClientTokenJwtAuthorizationFilterTest {
    @Mock
    private JwtCreationService jwtCreationService;

    @Mock
    private ClientTokenRepository clientTokenRepository;

    @Mock
    private SemaEventService semaEventService;

    @InjectMocks
    ClientTokenJwtAuthorizationFilter jwtAuthorizationFilter;

    @AfterAll
    static void afterAll() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldCorrectlyValidJwtTokenForClientToken() throws Exception {
        // Given
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        JwtClaims jwtClaims = new JwtClaims();
        jwtClaims.setJwtId(SOME_CLIENT_JWT_ID.toString());
        jwtClaims.setSubject(CLIENT_TOKEN_SUBJECT);
        jwtClaims.setClaim("scope", List.of(ClientTokenPermission.INVITE_USER));

        given(request.getHeader(HttpHeaders.AUTHORIZATION)).willReturn("Bearer SOME_ENCODED_JWT");
        given(jwtCreationService.getJwtIdFromDecryptedJwtWithoutValidation("SOME_ENCODED_JWT")).willReturn(SOME_CLIENT_JWT_ID);

        OffsetDateTime lastAccessedDateTime = OffsetDateTime.now();
        ClientTokenEntity clientToken = ClientTokenEntity.builder()
                .jwtId(SOME_CLIENT_JWT_ID)
                .signedPublicKeyId(SOME_JWT_PUBLIC_KEY_ID)
                .clientId(SOME_CLIENT_ID)
                .createdAdminEmail(SOME_CLIENT_ADMIN_EMAIL)
                .status(ClientTokenStatus.ACTIVE)
                .lastAccessedDate(lastAccessedDateTime)
                .permissions(List.of(INVITE_USER))
                .build();
        given(clientTokenRepository.findById(SOME_CLIENT_JWT_ID)).willReturn(Optional.of(clientToken));

        // When
        jwtAuthorizationFilter.doFilterInternal(request, response, filterChain);

        // Then
        then(filterChain).should().doFilter(request, response);
        then(clientTokenRepository).should().save(clientToken);
        then(semaEventService).should(never()).logClientTokenAccessToUnauthorizedEndpoint(any(), any(), any());
        UsernamePasswordAuthenticationToken auth = (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth.getPrincipal()).isEqualTo(
                ClientTokenPrincipal.builder()
                        .tokenId(SOME_CLIENT_JWT_ID)
                        .clientId(SOME_CLIENT_ID)
                        .email(SOME_CLIENT_ADMIN_EMAIL)
                        .build()
        );
        assertThat(auth.getAuthorities()).containsExactlyInAnyOrder(
                new SimpleGrantedAuthority(INVITE_USER.name()),
                new SimpleGrantedAuthority(SecurityRoles.ROLE_PREFIX + SecurityRoles.CLIENT_TOKEN));
    }

    @Test
    void shouldNotAuthenticateRevokeClientToken() throws Exception {
        // Given
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        JwtClaims jwtClaims = new JwtClaims();
        jwtClaims.setJwtId(SOME_CLIENT_JWT_ID.toString());
        jwtClaims.setSubject(CLIENT_TOKEN_SUBJECT);
        jwtClaims.setClaim("scope", List.of(ClientTokenPermission.INVITE_USER));

        given(request.getHeader(HttpHeaders.AUTHORIZATION)).willReturn("Bearer SOME_ENCODED_JWT");
        given(request.getRequestURI()).willReturn(INVITE_USER_CLIENT_TOKEN_ENDPOINT);
        given(jwtCreationService.getJwtClaimsFromDecryptedJwtAndPerformValidation("SOME_ENCODED_JWT")).willReturn(jwtClaims);

        ClientTokenEntity clientToken = ClientTokenEntity.builder()
                .jwtId(SOME_CLIENT_JWT_ID)
                .clientId(SOME_CLIENT_ID)
                .status(ClientTokenStatus.REVOKED)
                .build();
        given(clientTokenRepository.findById(SOME_CLIENT_JWT_ID)).willReturn(Optional.of(clientToken));

        given(response.getOutputStream()).willReturn(mock(ServletOutputStream.class));

        // When
        jwtAuthorizationFilter.doFilterInternal(request, response, filterChain);

        // Then
        then(filterChain).should(never()).doFilter(request, response);
        then(semaEventService).should(never()).logClientTokenAccessToUnauthorizedEndpoint(any(), any(), any());
        then(response).should().setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void shouldNotCreateSecurityContextWhenAuthorizationHeaderWillBeMissing() throws Exception {
        // Given
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        given(request.getHeader(HttpHeaders.AUTHORIZATION)).willReturn(null);
        given(response.getOutputStream()).willReturn(mock(ServletOutputStream.class));

        // When
        jwtAuthorizationFilter.doFilterInternal(request, response, filterChain);

        // Then
        then(filterChain).should(never()).doFilter(request, response);
        then(semaEventService).should(never()).logClientTokenAccessToUnauthorizedEndpoint(any(), any(), any());
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void shouldReturn401WhenTokenWouldNotBeValidatedCorrectly() throws Exception {
        // Given
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        OffsetDateTime lastAccessedDateTime = OffsetDateTime.now();
        ClientTokenEntity clientToken = ClientTokenEntity.builder()
                .jwtId(SOME_CLIENT_JWT_ID)
                .signedPublicKeyId(SOME_JWT_PUBLIC_KEY_ID)
                .clientId(SOME_CLIENT_ID)
                .createdAdminEmail(SOME_CLIENT_ADMIN_EMAIL)
                .status(ClientTokenStatus.ACTIVE)
                .lastAccessedDate(lastAccessedDateTime)
                .build();

        given(request.getHeader(HttpHeaders.AUTHORIZATION)).willReturn("SOME_ENCODED_JWT");
        given(jwtCreationService.getJwtIdFromDecryptedJwtWithoutValidation("SOME_ENCODED_JWT")).willReturn(SOME_CLIENT_JWT_ID);
        given(clientTokenRepository.findById(SOME_CLIENT_JWT_ID)).willReturn(Optional.of(clientToken));
        given(response.getOutputStream()).willReturn(mock(ServletOutputStream.class));

        // When
        jwtAuthorizationFilter.doFilterInternal(request, response, filterChain);

        // Then
        then(filterChain).should(never()).doFilter(request, response);
        then(semaEventService).should(never()).logClientTokenAccessToUnauthorizedEndpoint(any(), any(), any());
        then(response).should().setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
}
