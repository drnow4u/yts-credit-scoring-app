package com.yolt.creditscoring.configuration.security.user;

import com.yolt.creditscoring.exception.JwtCreationException;
import com.yolt.creditscoring.service.securitymodule.jwt.JwtCreationService;
import com.yolt.creditscoring.service.user.UserStorageService;
import com.yolt.creditscoring.service.user.model.CreditScoreUser;
import com.yolt.creditscoring.service.user.model.InvitationStatus;
import org.apache.http.HttpHeaders;
import org.jose4j.jwt.JwtClaims;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.FilterChain;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

import static com.yolt.creditscoring.TestUtils.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class UserJwtAuthorizationFilterTest {

    @Mock
    private JwtCreationService jwtCreationService;

    @Mock
    private UserStorageService userStorageService;

    @InjectMocks
    private UserJwtAuthorizationFilter jwtAuthorizationFilter;

    @AfterAll
    static void afterAll() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldCorrectlyValidJwtToken() throws Exception {
        // Given
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        JwtClaims jwtClaims = new JwtClaims();
        jwtClaims.setSubject(SOME_USER_HASH);

        given(request.getHeader(HttpHeaders.AUTHORIZATION)).willReturn("Bearer SOME_ENCODED_JWT");
        given(jwtCreationService.getJwtClaimsFromDecryptedJwtAndPerformValidation("SOME_ENCODED_JWT")).willReturn(jwtClaims);

        CreditScoreUser creditScoreUser = new CreditScoreUser();
        creditScoreUser.setId(SOME_USER_ID);
        creditScoreUser.setClientId(SOME_CLIENT_ID);
        creditScoreUser.setYoltUserId(SOME_YOLT_USER_ID);
        creditScoreUser.setYoltUserSiteId(SOME_YOLT_USER_SITE_ID);
        creditScoreUser.setStatus(InvitationStatus.INVITED);
        given(userStorageService.findByInvitationHash(SOME_USER_HASH)).willReturn(Optional.of(creditScoreUser));

        // When
        jwtAuthorizationFilter.doFilterInternal(request, response, filterChain);

        // Then
        then(filterChain).should().doFilter(request, response);
        UsernamePasswordAuthenticationToken auth = (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth.getPrincipal()).isEqualTo(
                CreditScoreUserPrincipal.builder()
                .userId(SOME_USER_ID)
                .clientId(SOME_CLIENT_ID)
                .yoltUserId(SOME_YOLT_USER_ID)
                .yoltUserSiteId(SOME_YOLT_USER_SITE_ID)
                .initRequestInvitationStatus(InvitationStatus.INVITED)
                .build()
        );
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
