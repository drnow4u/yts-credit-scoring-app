package com.yolt.creditscoring.configuration.security.admin;

import com.yolt.creditscoring.configuration.security.SecurityRoles;
import com.yolt.creditscoring.exception.OAuth2AuthenticationProcessingException;
import com.yolt.creditscoring.exception.OAuth2EmailMismatchException;
import com.yolt.creditscoring.exception.OAuth2NotRegisteredAdminException;
import com.yolt.creditscoring.service.clientadmin.model.AuthProvider;
import com.yolt.creditscoring.service.clientadmin.model.ClientAdmin;
import com.yolt.creditscoring.service.clientadmin.model.ClientAdminRepository;
import com.yolt.creditscoring.service.securitymodule.jwt.JwtCreationService;
import com.yolt.creditscoring.service.securitymodule.semaevent.SemaEventService;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.*;

import static com.yolt.creditscoring.TestUtils.SOME_CLIENT_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomOAuth2UserServiceTest {

    @Mock
    private ClientAdminRepository clientAdminRepository;

    @Mock
    private JwtCreationService jwtCreationService;

    @Mock
    private SemaEventService semaEventService;

    @Mock
    private TestCfaAdminProperties testCfaAdminProperties;

    @InjectMocks
    private CustomOAuth2UserService customOAuth2UserService;

    @BeforeEach
    public void before() {
        lenient().when(testCfaAdminProperties.getMicrosoftIds()).thenReturn(Collections.emptyList());
    }

    @Test
    void shouldProcessOAuth2UserForGithub() {
        // Given
        OAuth2User oAuth2User = mock(OAuth2User.class);
        given(oAuth2User.getAttributes()).willReturn(Map.of(
                "login", "some_login",
                "id", 123456789,
                "email", "some_email@test.com"
        ));

        OAuth2UserRequest oAuth2UserRequest = mock(OAuth2UserRequest.class);
        given(oAuth2UserRequest.getClientRegistration())
                .willReturn(createFakeClientRegistrationForGivenProvider("github"));

        given(clientAdminRepository.findByIdpIdAndAuthProvider("123456789", AuthProvider.GITHUB))
                .willReturn(Optional.of(new ClientAdmin()));

        // When
        OAuth2User result = customOAuth2UserService.processOAuth2User(oAuth2User, oAuth2UserRequest);

        // Then
        assertThat(result.getName()).isEqualTo("123456789");
        assertThat(result.getAttributes()).isEqualTo(Map.of(
                "idpId", "123456789",
                "login", "some_login",
                "id", 123456789,
                "email", "some_email@test.com"
        ));
        then(semaEventService).should(never()).logNotMatchingAdminEmailWithIdpId(any(),any(),any(),any(),any());
    }

    @Test
    void shouldProcessOAuth2UserForGoogle() {
        // Given
        OAuth2User oAuth2User = mock(OAuth2User.class);
        given(oAuth2User.getAttributes()).willReturn(Map.of(
                "sub", "1234567890987654321",
                "name", "Some Name",
                "email", "test_email@gmail.com"
        ));

        OAuth2UserRequest oAuth2UserRequest = mock(OAuth2UserRequest.class);
        given(oAuth2UserRequest.getClientRegistration())
                .willReturn(createFakeClientRegistrationForGivenProvider("google"));

        ClientAdmin clientAdmin = new ClientAdmin();
        clientAdmin.setEmail("Test_Email@gmail.com");
        given(clientAdminRepository.findByIdpIdAndAuthProvider("1234567890987654321", AuthProvider.GOOGLE))
                .willReturn(Optional.of(clientAdmin));

        // When
        OAuth2User result = customOAuth2UserService.processOAuth2User(oAuth2User, oAuth2UserRequest);

        // Then
        assertThat(result.getName()).isEqualTo("1234567890987654321");
        assertThat(result.getAttributes()).isEqualTo(Map.of(
                "idpId", "1234567890987654321",
                "sub", "1234567890987654321",
                "name", "Some Name",
                "email", "test_email@gmail.com"
        ));
        then(semaEventService).should(never()).logNotMatchingAdminEmailWithIdpId(any(),any(),any(),any(),any());
    }

    @Test
    void shouldThrowAnErrorAndLogSemaEventWhenEmailDoesNotMatchForGoogle() {
        // Given
        OAuth2User oAuth2User = mock(OAuth2User.class);
        given(oAuth2User.getAttributes()).willReturn(Map.of(
                "sub", "1234567890987654321",
                "name", "Some Name",
                "email", "test_email@gmail.com"
        ));

        OAuth2UserRequest oAuth2UserRequest = mock(OAuth2UserRequest.class);
        given(oAuth2UserRequest.getClientRegistration())
                .willReturn(createFakeClientRegistrationForGivenProvider("google"));

        ClientAdmin clientAdmin = new ClientAdmin();
        clientAdmin.setEmail("different_stored_email@gmail.com");
        clientAdmin.setClientId(SOME_CLIENT_ID);
        given(clientAdminRepository.findByIdpIdAndAuthProvider("1234567890987654321", AuthProvider.GOOGLE))
                .willReturn(Optional.of(clientAdmin));

        // When
        Throwable thrown = catchThrowable(() -> customOAuth2UserService.processOAuth2User(oAuth2User, oAuth2UserRequest));

        // Then
        assertThat(thrown).isInstanceOf(OAuth2EmailMismatchException.class);
        assertThat(thrown.getMessage()).isEqualTo("Email mismatch between stored client admin email and provided one");
    }

    @Test
    void shouldProcessOAuth2UserForMicrosoft() throws InvalidJwtException {
        // Given
        OAuth2User oAuth2User = mock(OAuth2User.class);
        given(oAuth2User.getAttributes()).willReturn(Map.of(
                "sub", "852468ff-31c6-4eab-b767-25a4100182dd",
                "name", "Some Name",
                "email", "test_email@outlook.com"
        ));

        OAuth2UserRequest oAuth2UserRequest = mock(OAuth2UserRequest.class);
        given(oAuth2UserRequest.getClientRegistration())
                .willReturn(createFakeClientRegistrationForGivenProvider("microsoft"));
        given(oAuth2UserRequest.getAdditionalParameters()).willReturn(Map.of("id_token", "some_access_token"));

        given(jwtCreationService.getJwtClaims("some_access_token"))
                .willReturn(JwtClaims.parse("{\"oid\":\"3cf96982-56c1-4732-b629-c5084c1a8eaa\", \"tid\": \"" + UUID.randomUUID().toString() + "\"}"));

        ClientAdmin clientAdmin = new ClientAdmin();
        clientAdmin.setEmail("Test_Email@outlook.com");
        given(clientAdminRepository.findByIdpIdAndAuthProvider("3cf96982-56c1-4732-b629-c5084c1a8eaa", AuthProvider.MICROSOFT))
                .willReturn(Optional.of(clientAdmin));

        // When
        OAuth2User result = customOAuth2UserService.processOAuth2User(oAuth2User, oAuth2UserRequest);

        // Then
        assertThat(result.getName()).isEqualTo("3cf96982-56c1-4732-b629-c5084c1a8eaa");
        assertThat(result.getAttributes()).isEqualTo(Map.of(
                "idpId", "3cf96982-56c1-4732-b629-c5084c1a8eaa",
                "sub", "852468ff-31c6-4eab-b767-25a4100182dd",
                "name", "Some Name",
                "email", "test_email@outlook.com"
        ));
        then(semaEventService).should(never()).logNotMatchingAdminEmailWithIdpId(any(),any(),any(),any(),any());
    }

    @Test
    void shouldThrowAnErrorAndLogSemaEventWhenEmailDoesNotMatchForMicrosoft() throws InvalidJwtException {
        // Given
        OAuth2User oAuth2User = mock(OAuth2User.class);
        given(oAuth2User.getAttributes()).willReturn(Map.of(
                "sub", "852468ff-31c6-4eab-b767-25a4100182dd",
                "name", "Some Name",
                "email", "test_email@outlook.com",
                "tid", UUID.randomUUID().toString()

        ));

        OAuth2UserRequest oAuth2UserRequest = mock(OAuth2UserRequest.class);
        given(oAuth2UserRequest.getClientRegistration())
                .willReturn(createFakeClientRegistrationForGivenProvider("microsoft"));
        given(oAuth2UserRequest.getAdditionalParameters()).willReturn(Map.of("id_token", "some_access_token"));

        given(jwtCreationService.getJwtClaims("some_access_token"))
                .willReturn(JwtClaims.parse("{\"oid\":\"3cf96982-56c1-4732-b629-c5084c1a8eaa\", \"tid\" : \"852468ff-31c6-4eab-b767-25a4100182dd\"}"));

        ClientAdmin clientAdmin = new ClientAdmin();
        clientAdmin.setEmail("different_stored_email@outlook.com");
        clientAdmin.setClientId(SOME_CLIENT_ID);
        given(clientAdminRepository.findByIdpIdAndAuthProvider("3cf96982-56c1-4732-b629-c5084c1a8eaa", AuthProvider.MICROSOFT))
                .willReturn(Optional.of(clientAdmin));

        // When
        Throwable thrown = catchThrowable(() -> customOAuth2UserService.processOAuth2User(oAuth2User, oAuth2UserRequest));

        // Then
        assertThat(thrown).isInstanceOf(OAuth2EmailMismatchException.class);
        assertThat(thrown.getMessage()).isEqualTo("Email mismatch between stored client admin email and provided one");
    }

    @Test
    void shouldThrowExceptionForMicrosoftProviderWhenOidIsMissing() throws InvalidJwtException {
        // Given
        OAuth2User oAuth2User = mock(OAuth2User.class);
        given(oAuth2User.getAttributes()).willReturn(Map.of(
                "sub", "852468ff-31c6-4eab-b767-25a4100182dd",
                "name", "Some Name",
                "email", "test_email@outlook.com",
                "tid", UUID.randomUUID().toString()

        ));

        OAuth2UserRequest oAuth2UserRequest = mock(OAuth2UserRequest.class);
        given(oAuth2UserRequest.getClientRegistration())
                .willReturn(createFakeClientRegistrationForGivenProvider("microsoft"));
        given(oAuth2UserRequest.getAdditionalParameters()).willReturn(Map.of("id_token", "some_access_token"));

        given(jwtCreationService.getJwtClaims("some_access_token"))
                .willReturn(JwtClaims.parse("{\"not_oid\":\"12345\"}"));

        // When
        Throwable thrown = catchThrowable(() -> customOAuth2UserService.processOAuth2User(oAuth2User, oAuth2UserRequest));

        // Then
        assertThat(thrown).isInstanceOf(OAuth2AuthenticationProcessingException.class);
        assertThat(thrown.getMessage()).isEqualTo("ID parameter not present in oauth user for provider: MICROSOFT");
        then(semaEventService).should(never()).logNotMatchingAdminEmailWithIdpId(any(),any(),any(),any(),any());
    }

    @Test
    void shouldThrowExceptionForWhenUserIsMissingInDatabase() {
        // Given
        OAuth2User oAuth2User = mock(OAuth2User.class);
        given(oAuth2User.getAttributes()).willReturn(Map.of(
                "sub", "1234567890987654321",
                "email", "test_email@gmail.com"
        ));

        OAuth2UserRequest oAuth2UserRequest = mock(OAuth2UserRequest.class);
        given(oAuth2UserRequest.getClientRegistration())
                .willReturn(createFakeClientRegistrationForGivenProvider("google"));

        given(clientAdminRepository.findByIdpIdAndAuthProvider("1234567890987654321", AuthProvider.GOOGLE))
                .willReturn(Optional.empty());

        // When
        Throwable thrown = catchThrowable(() -> customOAuth2UserService.processOAuth2User(oAuth2User, oAuth2UserRequest));

        // Then
        assertThat(thrown).isInstanceOf(OAuth2NotRegisteredAdminException.class);
        assertThat(((OAuth2NotRegisteredAdminException)thrown).getIdpId()).isEqualTo("1234567890987654321");
        assertThat(((OAuth2NotRegisteredAdminException)thrown).getProvider()).isEqualTo("GOOGLE");
        then(semaEventService).should(never()).logNotMatchingAdminEmailWithIdpId(any(),any(),any(),any(),any());
    }

    @Test
    void shouldThrowExceptionForMissingIdTokenParameter() {
        // Given
        OAuth2User oAuth2User = mock(OAuth2User.class);
        given(oAuth2User.getAttributes()).willReturn(Map.of(
                "sub", "1234567890987654321",
                "name", "Some Name",
                "email", "test_email@gmail.com",
                "tid", UUID.randomUUID().toString()

        ));

        OAuth2UserRequest oAuth2UserRequest = mock(OAuth2UserRequest.class);
        given(oAuth2UserRequest.getClientRegistration())
                .willReturn(createFakeClientRegistrationForGivenProvider("microsoft"));

        given(oAuth2UserRequest.getAdditionalParameters()).willReturn(Map.of("missing_id_token", "some_access_token"));


        // When
        Throwable thrown = catchThrowable(() -> customOAuth2UserService.processOAuth2User(oAuth2User, oAuth2UserRequest));

        // Then
        assertThat(thrown).isInstanceOf(OAuth2AuthenticationProcessingException.class);
        assertThat(thrown.getMessage()).isEqualTo("The id token is missing for Microsoft authentication");
        then(semaEventService).should(never()).logNotMatchingAdminEmailWithIdpId(any(),any(),any(),any(),any());
    }

    @Test
    public void given_aUserInYoltTenantWithCFAAdminSecurityGroup_then_heShouldGetTheCFAAdminRole() throws InvalidJwtException {
        // Given
        OAuth2User oAuth2User = mock(OAuth2User.class);
        given(oAuth2User.getAttributes()).willReturn(Map.of(
                "sub", "852468ff-31c6-4eab-b767-25a4100182dd",
                "name", "Some Name",
                "email", "test_email@outlook.com"
        ));

        OAuth2UserRequest oAuth2UserRequest = mock(OAuth2UserRequest.class);
        given(oAuth2UserRequest.getClientRegistration())
                .willReturn(createFakeClientRegistrationForGivenProvider("microsoft"));
        given(oAuth2UserRequest.getAdditionalParameters()).willReturn(Map.of("id_token", "some_access_token"));

        String yoltTenant = "21707a2b-2fc1-4196-a147-bdbd9f732618";
        String cfaSecurityGroup = "16d851d1-cd24-4f80-9d01-6b14b8ef1852";
        given(jwtCreationService.getJwtClaims("some_access_token"))
                .willReturn(JwtClaims.parse("{\"oid\":\"3cf96982-56c1-4732-b629-c5084c1a8eaa\", \"tid\": \"%s\", \"groups\" : [\"%s\"]}".formatted(yoltTenant, cfaSecurityGroup)));

        given(clientAdminRepository.findByIdpIdAndAuthProvider(any(), eq(AuthProvider.MICROSOFT)))
                .willReturn(Optional.empty());

        // When
        OAuth2AdminUser result = customOAuth2UserService.processOAuth2User(oAuth2User, oAuth2UserRequest);

        // Then
        assertThat(result.getName()).isEqualTo("3cf96982-56c1-4732-b629-c5084c1a8eaa");
        assertThat(result.getAttributes()).isEqualTo(Map.of(
                "idpId", "3cf96982-56c1-4732-b629-c5084c1a8eaa",
                "sub", "852468ff-31c6-4eab-b767-25a4100182dd",
                "name", "Some Name",
                "email", "test_email@outlook.com"
        ));
        assertThat(result.getAuthorities().stream().map(GrantedAuthority::getAuthority)).containsOnlyOnce(SecurityRoles.ROLE_PREFIX + SecurityRoles.CFA_ADMIN);
    }

    @Test
    public void given_aUserThatIsWhitelistedForCFAAdminOnTestEnvironments_then_heShouldGetTheCFAAdminRole() throws InvalidJwtException {
        // Given
        OAuth2User oAuth2User = mock(OAuth2User.class);
        given(oAuth2User.getAttributes()).willReturn(Map.of(
                "sub", "852468ff-31c6-4eab-b767-25a4100182dd",
                "name", "Some Name",
                "email", "test_email@outlook.com"
        ));

        OAuth2UserRequest oAuth2UserRequest = mock(OAuth2UserRequest.class);
        given(oAuth2UserRequest.getClientRegistration())
                .willReturn(createFakeClientRegistrationForGivenProvider("microsoft"));
        given(oAuth2UserRequest.getAdditionalParameters()).willReturn(Map.of("id_token", "some_access_token"));

        given(jwtCreationService.getJwtClaims("some_access_token"))
                .willReturn(JwtClaims.parse("{\"oid\":\"3cf96982-56c1-4732-b629-c5084c1a8eaa\", \"tid\": \"" + UUID.randomUUID() + "\"}"));

        given(clientAdminRepository.findByIdpIdAndAuthProvider(any(), eq(AuthProvider.MICROSOFT)))
                .willReturn(Optional.empty());

        given(testCfaAdminProperties.getMicrosoftIds()).willReturn(List.of("3cf96982-56c1-4732-b629-c5084c1a8eaa"));
        // When
        OAuth2AdminUser result = customOAuth2UserService.processOAuth2User(oAuth2User, oAuth2UserRequest);

        // Then
        assertThat(result.getName()).isEqualTo("3cf96982-56c1-4732-b629-c5084c1a8eaa");
        assertThat(result.getAttributes()).isEqualTo(Map.of(
                "idpId", "3cf96982-56c1-4732-b629-c5084c1a8eaa",
                "sub", "852468ff-31c6-4eab-b767-25a4100182dd",
                "name", "Some Name",
                "email", "test_email@outlook.com"
        ));
        assertThat(result.getAuthorities().stream().map(GrantedAuthority::getAuthority)).containsOnlyOnce(SecurityRoles.ROLE_PREFIX + SecurityRoles.CFA_ADMIN);
    }

    private ClientRegistration createFakeClientRegistrationForGivenProvider(String provider) {
        return ClientRegistration.withRegistrationId(provider)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .clientId("test")
                .redirectUri("test")
                .tokenUri("test")
                .build();
    }
}
