package com.yolt.creditscoring.configuration.security.admin;

import com.yolt.creditscoring.configuration.security.SecurityRoles;
import com.yolt.creditscoring.exception.JwtCreationException;
import com.yolt.creditscoring.service.clientadmin.model.ClientAdmin;
import com.yolt.creditscoring.service.clientadmin.model.ClientAdminRepository;
import com.yolt.creditscoring.service.securitymodule.jwt.JwtCreationService;
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
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.yolt.creditscoring.TestUtils.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class ClientAdminJwtAuthorizationFilterTest {

    @Mock
    private JwtCreationService jwtCreationService;

    @InjectMocks
    ClientAdminJwtAuthorizationFilter jwtAuthorizationFilter;

    @AfterAll
    static void afterAll() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldCorrectlyValidJwtTokenForClientAdmin() throws Exception {
        // Given
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        JwtClaims jwtClaims = new JwtClaims();
        jwtClaims.setSubject(SOME_CLIENT_ADMIN_IDP_ID);
        jwtClaims.setClaim("roles", List.of("ROLE_CLIENT_ADMIN"));
        jwtClaims.setClaim("email", SOME_USER_EMAIL);
        jwtClaims.setClaim("idpId", SOME_CLIENT_ADMIN_IDP_ID);
        jwtClaims.setClaim("clientId", SOME_CLIENT_ID);
        jwtClaims.setClaim("userId", SOME_USER_ID);

        given(request.getHeader(HttpHeaders.AUTHORIZATION)).willReturn("Bearer SOME_ENCODED_JWT");
        given(jwtCreationService.getJwtClaimsFromDecryptedJwtAndPerformValidation("SOME_ENCODED_JWT")).willReturn(jwtClaims);

        // When
        jwtAuthorizationFilter.doFilterInternal(request, response, filterChain);

        // Then
        then(filterChain).should().doFilter(request, response);
        UsernamePasswordAuthenticationToken auth = (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth.getPrincipal()).isEqualTo(jwtClaims);
        assertThat(auth.getAuthorities()).containsOnly(
                new SimpleGrantedAuthority(SecurityRoles.ROLE_PREFIX + SecurityRoles.CLIENT_ADMIN));
    }

    @Test
    void shouldNotCreateSecurityContextWhenAuthorizationHeaderWillBeMissing() throws Exception {
        // Given
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        given(request.getHeader(HttpHeaders.AUTHORIZATION)).willReturn(null);

        // When
        jwtAuthorizationFilter.doFilterInternal(request, response, filterChain);

        // Then
        then(filterChain).should().doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void shouldReturn401WhenTokenWouldNotBeValidatedCorrectly() throws Exception {
        // Given
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        given(request.getHeader(HttpHeaders.AUTHORIZATION)).willReturn("SOME_ENCODED_JWT");
        given(jwtCreationService.getJwtClaimsFromDecryptedJwtAndPerformValidation("SOME_ENCODED_JWT"))
                .willThrow(new JwtCreationException("There was an error decrypting JWT"));
        given(response.getOutputStream()).willReturn(mock(ServletOutputStream.class));

        // When
        jwtAuthorizationFilter.doFilterInternal(request, response, filterChain);

        // Then
        then(filterChain).should(never()).doFilter(request, response);
        then(response).should().setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
}
