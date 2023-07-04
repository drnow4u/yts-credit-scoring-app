package com.yolt.creditscoring.configuration.security.user;

import com.yolt.creditscoring.service.user.UserStorageService;
import com.yolt.creditscoring.service.user.model.CreditScoreUserRepository;
import com.yolt.creditscoring.service.user.model.InvitationStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.FilterChain;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.yolt.creditscoring.TestUtils.SOME_CLIENT_ID;
import static com.yolt.creditscoring.TestUtils.SOME_USER_ID;
import static com.yolt.creditscoring.controller.user.client.ClientController.CLIENT_ENDPOINT;
import static com.yolt.creditscoring.controller.user.invitation.UserInvitationController.USER_CONSENT_ENDPOINT;
import static com.yolt.creditscoring.controller.user.legaldocument.LegalDocumentController.TERMS_CONDITIONS_ENDPOINT;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class UserFlowVerificationFilterTest {

    private UserFlowVerificationFilter userFlowVerificationFilter;

    @Mock
    CreditScoreUserRepository creditScoreUserRepository;

    @BeforeEach
    void setUp() {
        UserStorageService userStorageService = new UserStorageService(creditScoreUserRepository);

        userFlowVerificationFilter = new UserFlowVerificationFilter(userStorageService);
    }

    @AfterEach
    void afterAll() {
        // Setting global variable - can affect other spring integration tests
        // We need to clear the security context in the afterAll method
        SecurityContextHolder.clearContext();
    }

    @ParameterizedTest
    @ValueSource(strings = {TERMS_CONDITIONS_ENDPOINT, CLIENT_ENDPOINT, USER_CONSENT_ENDPOINT})
    void shouldNotApplyFilterToSpecifiedEndpoints(String endpoint) throws Exception {
        // Given
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);
        given(request.getRequestURI()).willReturn(endpoint);

        // When
        userFlowVerificationFilter.doFilterInternal(request, response, filterChain);

        // Then
        then(response).should(never()).setStatus(HttpServletResponse.SC_FORBIDDEN);
        then(response).should(never()).sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden");
    }

    @Test
    void shouldReturnErrorWhenFilterWouldBeAppliedAndInvitationStatus() throws Exception {
        // Given
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        given(securityContext.getAuthentication()).willReturn(authentication);
        given(authentication.getPrincipal())
                .willReturn(CreditScoreUserPrincipal.builder()
                        .userId(SOME_USER_ID)
                        .clientId(SOME_CLIENT_ID)
                        .initRequestInvitationStatus(InvitationStatus.COMPLETED)
                        .build());

        SecurityContextHolder.setContext(securityContext);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);
        given(request.getRequestURI()).willReturn("/api/not-listed-endpoint");
        given(response.getOutputStream()).willReturn(mock(ServletOutputStream.class));

        // When
        userFlowVerificationFilter.doFilterInternal(request, response, filterChain);

        // Then
        then(response).should().setStatus(HttpServletResponse.SC_BAD_REQUEST);
    }

    @Test
    void shouldNotApplyFilterToUnauthorizedUser() throws Exception {
        // Given
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);
        given(request.getRequestURI()).willReturn("/api/token");

        // When
        userFlowVerificationFilter.doFilterInternal(request, response, filterChain);

        // Then
        then(response).should(never()).setStatus(HttpServletResponse.SC_FORBIDDEN);
        then(response).should(never()).sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden");
    }
}
