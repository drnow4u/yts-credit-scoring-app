package com.yolt.creditscoring.controller.customer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.creditscoring.IntegrationTest;
import com.yolt.creditscoring.configuration.security.SecurityRoles;
import com.yolt.creditscoring.controller.admin.clienttoken.ClientTokenController;
import com.yolt.creditscoring.service.audit.AdminAuditService;
import com.yolt.creditscoring.service.clienttoken.CreateClientTokenRequestDTO;
import com.yolt.creditscoring.service.clienttoken.model.ClientTokenEntity;
import com.yolt.creditscoring.service.clienttoken.model.ClientTokenPermission;
import com.yolt.creditscoring.service.clienttoken.model.ClientTokenRepository;
import com.yolt.creditscoring.service.clienttoken.model.ClientTokenStatus;
import com.yolt.creditscoring.service.securitymodule.jwt.JwtCreationService;
import com.yolt.creditscoring.service.securitymodule.jwt.JwtEncryption;
import com.yolt.creditscoring.service.securitymodule.semaevent.SemaEventService;
import com.yolt.creditscoring.service.securitymodule.signature.PublicKeyEntity;
import com.yolt.creditscoring.service.securitymodule.signature.PublicKeyRepository;
import com.yolt.creditscoring.service.securitymodule.vault.VaultSecretKeyService;
import com.yolt.creditscoring.service.user.model.CreditScoreUserRepository;
import com.yolt.creditscoring.service.userjourney.model.UserJourneyRepository;
import org.jose4j.jwk.RsaJwkGenerator;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static com.yolt.creditscoring.TestUtils.*;
import static com.yolt.creditscoring.configuration.security.admin.ClientAccessType.TOKEN;
import static com.yolt.creditscoring.configuration.security.admin.TestUtils.OAUTH_ADMIN_USER_CLIENT_ADMIN;
import static com.yolt.creditscoring.controller.SecurityHelper.hasSecurityHeaderSetup;
import static com.yolt.creditscoring.controller.admin.clienttoken.ClientTokenController.CREATE_TOKEN_ENDPOINT;
import static com.yolt.creditscoring.controller.customer.CustomerAPIController.DELETE_USER_ENDPOINT;
import static com.yolt.creditscoring.controller.customer.CustomerAPIController.INVITE_USER_CLIENT_TOKEN_ENDPOINT;
import static com.yolt.creditscoring.service.clienttoken.model.ClientTokenPermission.*;
import static com.yolt.creditscoring.service.securitymodule.jwt.JwtCreationService.TOKEN_EXPIRATION_TIME_MINUTES;
import static com.yolt.creditscoring.service.securitymodule.jwt.JwtCreationService.TOKEN_TYPE_PREFIX;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
class CustomerAPIControllerIT {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private JwtCreationService jwtCreationService;

    @Autowired
    private CreditScoreUserRepository creditScoreUserRepository;

    @Autowired
    private JwtEncryption jwtEncryption;

    @Autowired
    private ClientTokenRepository clientTokenRepository;

    @Autowired
    private PublicKeyRepository publicKeyRepository;

    @Autowired
    private UserJourneyRepository userJourneyRepository;

    @MockBean
    private SemaEventService semaEventService;

    @MockBean
    private AdminAuditService adminAuditService;

    @SpyBean
    VaultSecretKeyService vaultSecretKeyService;

    @AfterEach
    void afterTest() {
        creditScoreUserRepository.deleteAll();
        clientTokenRepository.deleteAll();
        userJourneyRepository.deleteAll();
    }

    @Test
    void shouldClientWithSingleEmailTemplateInviteUser() throws Exception {
        // Given
        var clientToken = createClientToken(INVITE_USER, DOWNLOAD_REPORT);

        // When
        var perform = mvc.perform(post(INVITE_USER_CLIENT_TOKEN_ENDPOINT)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + clientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        String.format("""
                                {
                                    "name": "%s",
                                    "email": "%s"
                                }
                                """, SOME_USER_NAME, SOME_USER_EMAIL)));
        // Then
        var mapper = new ObjectMapper();
        var inviteUserResponse = perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId", notNullValue()))
                .andReturn()
                .getResponse();
        var inviteUserDTO =
                mapper.readValue(inviteUserResponse.getContentAsString(), CustomerAPIUserInvitationDTO.class);

        var user = creditScoreUserRepository.findById(inviteUserDTO.userId());
        assertThat(user).get().extracting("name", "email")
                .contains(SOME_USER_NAME, SOME_USER_EMAIL);

        then(adminAuditService).should(times(1))
                .inviteNewUser(
                        eq(SOME_CLIENT_ID), any(), eq("adminuser@test.com"),
                        any(), eq(SOME_USER_NAME), eq(SOME_USER_EMAIL), eq(TOKEN));

        then(semaEventService).should()
                .logUserInvitation(eq(SOME_CLIENT_ID), any());
    }

    @Test
    void shouldClientWithSingleEmailTemplateInviteUserWhenJWTSigningKeyRotate() throws Exception {
        // Given
        var clientToken = createClientToken(INVITE_USER, DOWNLOAD_REPORT);

        var rsaJsonWebKeyNew = RsaJwkGenerator.generateJwk(2048);
        var signingKeyIdNew = UUID.fromString("6196257e-58f6-4e29-8d7f-31e00867b214");
        rsaJsonWebKeyNew.setKeyId(signingKeyIdNew.toString());
        given(vaultSecretKeyService.getJwtSignKeyId()).willReturn(signingKeyIdNew);
        given(vaultSecretKeyService.getJwtSigningPublicKey()).willReturn(rsaJsonWebKeyNew.getRsaPublicKey());

        // When
        var perform = mvc.perform(post(INVITE_USER_CLIENT_TOKEN_ENDPOINT)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + clientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        String.format("""
                                {
                                    "name": "%s",
                                    "email": "%s"
                                }
                                """, SOME_USER_NAME, SOME_USER_EMAIL)));
        // Then
        var mapper = new ObjectMapper();
        var inviteUserResponse = perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId", notNullValue()))
                .andReturn()
                .getResponse();
        var inviteUserDTO =
                mapper.readValue(inviteUserResponse.getContentAsString(), CustomerAPIUserInvitationDTO.class);

        var user = creditScoreUserRepository.findById(inviteUserDTO.userId());
        assertThat(user).get().extracting("name", "email")
                .contains(SOME_USER_NAME, SOME_USER_EMAIL);

        then(adminAuditService).should(times(1))
                .inviteNewUser(
                        eq(SOME_CLIENT_ID), any(), eq("adminuser@test.com"),
                        any(), eq(SOME_USER_NAME), eq(SOME_USER_EMAIL), eq(TOKEN));

        then(semaEventService).should()
                .logUserInvitation(eq(SOME_CLIENT_ID), any());
    }

    @Test
    void shouldClientWithMultipleEmailTemplateInviteUser() throws Exception {
        // Given
        var clientToken = createClientToken(INVITE_USER, DOWNLOAD_REPORT);

        // When
        var perform = mvc.perform(post(INVITE_USER_CLIENT_TOKEN_ENDPOINT)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + clientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        String.format("""
                                {
                                    "name": "%s",
                                    "email": "%s",
                                    "clientEmailId": "%s"
                                }
                                """, SOME_USER_NAME, SOME_USER_EMAIL, SOME_CLIENT_EMAIL_ID)));
        // Then
        var mapper = new ObjectMapper();
        var inviteUserResponse = perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId", notNullValue()))
                .andReturn()
                .getResponse();
        var inviteUserDTO =
                mapper.readValue(inviteUserResponse.getContentAsString(), CustomerAPIUserInvitationDTO.class);

        var user = creditScoreUserRepository.findById(inviteUserDTO.userId());
        assertThat(user).get().extracting("name", "email")
                .contains(SOME_USER_NAME, SOME_USER_EMAIL);

        then(adminAuditService).should(times(1))
                .inviteNewUser(
                        eq(SOME_CLIENT_ID), any(), eq("adminuser@test.com"),
                        any(), eq(SOME_USER_NAME), eq(SOME_USER_EMAIL), eq(TOKEN));

        then(semaEventService).should()
                .logUserInvitation(eq(SOME_CLIENT_ID), any());
    }

    @Test
    void shouldNotInviteUserWithoutInvitePermission() throws Exception {
        // Given
        var clientToken = createClientToken(DOWNLOAD_REPORT, DELETE_USER);

        // When
        var perform = mvc.perform(post(INVITE_USER_CLIENT_TOKEN_ENDPOINT)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + clientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        String.format("""
                                {
                                    "name": "%s",
                                    "email": "%s"
                                }
                                """, SOME_USER_NAME, SOME_USER_EMAIL)));
        // Then
        perform
                .andExpect(status().isForbidden());

        var users = creditScoreUserRepository.findAll();
        assertThat(users).isEmpty();

        then(adminAuditService).should(never())
                .inviteNewUser(any(), any(), any(), any(), any(), any(), any());

        then(semaEventService).should()
                .logClientTokenAccessToUnauthorizedEndpoint(eq(SOME_CLIENT_ID), eq("/api/customer/users/invite"),
                        argThat(list -> list.containsAll(List.of(new SimpleGrantedAuthority(DOWNLOAD_REPORT.name()), new SimpleGrantedAuthority(SecurityRoles.ROLE_PREFIX + SecurityRoles.CLIENT_TOKEN)))));
    }

    @Test
    void shouldNotInviteUserWithValidClientAdminToken() throws Exception {
        // When
        var perform = mvc.perform(post(INVITE_USER_CLIENT_TOKEN_ENDPOINT)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtCreationService.createAdminToken(OAUTH_ADMIN_USER_CLIENT_ADMIN))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        String.format("""
                                {
                                    "name": "%s",
                                    "email": "%s"
                                }
                                """, SOME_USER_NAME, SOME_USER_EMAIL)));
        // Then
        perform
                .andExpect(status().isUnauthorized());

        var users = creditScoreUserRepository.findAll();
        assertThat(users).isEmpty();

        then(adminAuditService).should(never())
                .inviteNewUser(any(), any(), any(), any(), any(), any(), any());

        then(semaEventService).should(never())
                .logClientTokenAccessToUnauthorizedEndpoint(any(), any(), any());
    }

    /**
     * This test is to check that verification of JWT signature is correctly setup for authorization header
     * <p>
     * Currently, authorization token is signed then encrypted JWT. Usually in OAuth2 is only signed JWT.
     * For only signed JWT, the id of signing key and claims are in the plan text and public visible.
     * In such scenario, private signing key with same keyId can be generated and used to sign malicious authorization JWT.
     */
    @Test
    void shouldNotInviteUserWithValidTokenSignedByOtherAuthority() throws Exception {
        // Given
        var rsaJsonWebKey = RsaJwkGenerator.generateJwk(2048);
        var signingKeyId = UUID.randomUUID();
        rsaJsonWebKey.setKeyId(signingKeyId.toString());

        final var publicKeyEntity = new PublicKeyEntity();
        publicKeyEntity.setKid(UUID.fromString(rsaJsonWebKey.getKeyId()));
        publicKeyEntity.setCreatedDate(OffsetDateTime.now());
        publicKeyEntity.setPublicKey(rsaJsonWebKey.getRsaPublicKey().getEncoded());

        publicKeyRepository.save(publicKeyEntity);

        var clientTokenEntity = ClientTokenEntity.builder()
                .jwtId(UUID.randomUUID())
                .clientId(SOME_CLIENT_ID)
                .signedPublicKeyId(signingKeyId)
                .name("Testing token")
                .permissions(List.of(INVITE_USER))
                .expirationDate(OffsetDateTime.now().plusDays(10))
                .createdDate(OffsetDateTime.now())
                .createdAdminEmail(SOME_CLIENT_ADMIN_EMAIL)
                .status(ClientTokenStatus.ACTIVE)
                .build();
        clientTokenRepository.save(clientTokenEntity);

        var claims = new JwtClaims();
        claims.setSubject(SOME_CLIENT_ADMIN_IDP_ID);
        claims.setIssuedAtToNow();
        claims.setJwtId(clientTokenEntity.getJwtId().toString());
        claims.setExpirationTimeMinutesInTheFuture(TOKEN_EXPIRATION_TIME_MINUTES);
        claims.setClaim("scope", List.of(INVITE_USER));

        var jws = new JsonWebSignature();
        jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.RSA_USING_SHA512);
        jws.setPayload(claims.toJson());

        var rsaJsonWebKeyFake = RsaJwkGenerator.generateJwk(2048);
        rsaJsonWebKeyFake.setKeyId(signingKeyId.toString());

        jws.setKey(rsaJsonWebKeyFake.getRsaPrivateKey());

        var token = TOKEN_TYPE_PREFIX + " " + jwtEncryption.encrypt(jws.getCompactSerialization());

        // When
        var perform = mvc.perform(post(INVITE_USER_CLIENT_TOKEN_ENDPOINT)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        String.format("""
                                {
                                    "name": "%s",
                                    "email": "%s"
                                }
                                """, SOME_USER_NAME, SOME_USER_EMAIL)));
        // Then
        perform
                .andExpect(status().isUnauthorized());

        var users = creditScoreUserRepository.findAll();
        assertThat(users).isEmpty();

        then(adminAuditService).should(never())
                .inviteNewUser(any(), any(), any(), any(), any(), any(), any());

        then(semaEventService).should(never())
                .logClientTokenAccessToUnauthorizedEndpoint(any(), any(), any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"X", "<h1>John Doe</h1>", " ", USER_NAME_LONGER_256})
    void shouldReturnViolationMessagesWhenInviteUserWithNotValidName(String name) throws Exception {
        // Given
        var clientToken = createClientToken(INVITE_USER, DOWNLOAD_REPORT);

        // When
        var result = mvc.perform(post(INVITE_USER_CLIENT_TOKEN_ENDPOINT)
                .header(HttpHeaders.AUTHORIZATION, clientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT_LANGUAGE, "pl")
                .content(String.format("{ \"name\": \"%s\", \"email\": \"%s\", \"clientEmailId\": \"%s\" }", name, SOME_USER_EMAIL, SOME_CLIENT_EMAIL_ID)));

        // Then
        hasSecurityHeaderSetup(result)
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.violations.[0].fieldName", equalTo("name")))
                .andExpect(jsonPath("$.violations.[0].message", equalTo("must be a well-formed user name")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"user@example.com.", "user@example..com", "user@.example.com", "user@@example.com", "<h1>user</h1>@example.com"})
    void shouldReturnViolationMessagesWhenInviteUserWithNotValidEmail(String email) throws Exception {
        // Given
        var clientToken = createClientToken(INVITE_USER, DOWNLOAD_REPORT);

        // When
        var result = mvc.perform(post(INVITE_USER_CLIENT_TOKEN_ENDPOINT)
                .header(HttpHeaders.AUTHORIZATION, clientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT_LANGUAGE, "pl")
                .content(String.format("{ \"name\": \"%s\", \"email\": \"%s\", \"clientEmailId\": \"%s\" }", SOME_USER_NAME, email, SOME_CLIENT_EMAIL_ID)));

        // Then
        hasSecurityHeaderSetup(result)
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.violations.[0].fieldName", equalTo("email")))
                .andExpect(jsonPath("$.violations.[0].message", equalTo("must be a well-formed email address")));
    }

    @Test
    void shouldDeleteUserWhenTokenHasDeletePermission() throws Exception {
        // Given
        var clientToken = createClientToken(INVITE_USER, DELETE_USER);
        var inviteUser = mvc.perform(post(INVITE_USER_CLIENT_TOKEN_ENDPOINT)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + clientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        String.format("""
                                {
                                    "name": "%s",
                                    "email": "%s",
                                    "clientEmailId": "%s"
                                }
                                """, SOME_USER_NAME, SOME_USER_EMAIL, SOME_CLIENT_EMAIL_ID)));
        // Then
        var mapper = new ObjectMapper();
        var inviteUserResponse = inviteUser.andReturn().getResponse();
        var invitedUserDTO =
                mapper.readValue(inviteUserResponse.getContentAsString(), CustomerAPIUserInvitationDTO.class);

        assertThat(creditScoreUserRepository.findById(invitedUserDTO.userId())).isPresent();

        // When
        var perform = mvc.perform(delete(DELETE_USER_ENDPOINT, invitedUserDTO.userId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + clientToken));

        // Then
        perform.andExpect(status().isOk());

        assertThat(creditScoreUserRepository.findById(invitedUserDTO.userId())).isNotPresent();

        then(adminAuditService).should().deleteUser(eq(SOME_CLIENT_ID), any(), eq("adminuser@test.com"),
                eq(invitedUserDTO.userId()), eq(SOME_USER_EMAIL), eq(TOKEN));
    }

    @Test
    void shouldNotDeleteUserWithoutDeletePermission() throws Exception {
        // Given
        var clientToken = createClientToken(INVITE_USER, DOWNLOAD_REPORT);

        // When
        var perform = mvc.perform(delete(DELETE_USER_ENDPOINT, UUID.randomUUID())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + clientToken));

        // Then
        perform.andExpect(status().isForbidden());

        then(adminAuditService).should(never())
                .deleteUser(any(), any(), any(), any(), any(), any());
    }

    private String createClientToken(final ClientTokenPermission... permissions) throws Exception {
        var mapper = new ObjectMapper();

        var performToken = mvc.perform(post(CREATE_TOKEN_ENDPOINT)
                .header(HttpHeaders.AUTHORIZATION, jwtCreationService.createAdminToken(OAUTH_ADMIN_USER_CLIENT_ADMIN))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(new CreateClientTokenRequestDTO("JWT", asList(permissions)))));
        var contentAsString = performToken.andReturn().getResponse().getContentAsString();

        return mapper.readValue(contentAsString, ClientTokenController.ClientTokenResponse.class)
                .clientToken();
    }
}
