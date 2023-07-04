package com.yolt.creditscoring.controller.customer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.creditscoring.IntegrationTest;
import com.yolt.creditscoring.configuration.security.SecurityRoles;
import com.yolt.creditscoring.controller.admin.clienttoken.ClientTokenController;
import com.yolt.creditscoring.service.audit.AdminAuditService;
import com.yolt.creditscoring.service.client.model.ClientEntity;
import com.yolt.creditscoring.service.client.model.ClientLanguage;
import com.yolt.creditscoring.service.client.model.ClientRepository;
import com.yolt.creditscoring.service.clientadmin.model.AuthProvider;
import com.yolt.creditscoring.service.clientadmin.model.ClientAdmin;
import com.yolt.creditscoring.service.clientadmin.model.ClientAdminRepository;
import com.yolt.creditscoring.service.clienttoken.model.ClientTokenEntity;
import com.yolt.creditscoring.service.clienttoken.model.ClientTokenPermission;
import com.yolt.creditscoring.service.clienttoken.model.ClientTokenRepository;
import com.yolt.creditscoring.service.clienttoken.model.ClientTokenStatus;
import com.yolt.creditscoring.service.creditscore.model.*;
import com.yolt.creditscoring.service.creditscore.recurringtransactions.RecurringTransactionsMonthlyReportEntity;
import com.yolt.creditscoring.service.creditscore.recurringtransactions.RecurringTransactionsMonthlyReportRepository;
import com.yolt.creditscoring.service.estimate.provider.dto.RiskClassification;
import com.yolt.creditscoring.service.estimate.storage.EstimateEntity;
import com.yolt.creditscoring.service.estimate.storage.EstimateRepository;
import com.yolt.creditscoring.service.securitymodule.jwt.JwtCreationService;
import com.yolt.creditscoring.service.securitymodule.jwt.JwtEncryption;
import com.yolt.creditscoring.service.securitymodule.semaevent.SemaEventService;
import com.yolt.creditscoring.service.securitymodule.signature.PublicKeyEntity;
import com.yolt.creditscoring.service.securitymodule.signature.PublicKeyRepository;
import com.yolt.creditscoring.service.securitymodule.vault.VaultSecretKeyService;
import com.yolt.creditscoring.service.user.model.CreditScoreUser;
import com.yolt.creditscoring.service.user.model.CreditScoreUserRepository;
import com.yolt.creditscoring.service.user.model.InvitationStatus;
import com.yolt.creditscoring.service.userjourney.model.UserJourneyRepository;
import lombok.NonNull;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.jwk.RsaJwkGenerator;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

import static com.yolt.creditscoring.TestUtils.*;
import static com.yolt.creditscoring.configuration.security.admin.TestUtils.createOauth2AdminUser;
import static com.yolt.creditscoring.controller.SecurityHelper.hasSecurityHeaderSetup;
import static com.yolt.creditscoring.controller.admin.clienttoken.ClientTokenController.CREATE_TOKEN_ENDPOINT;
import static com.yolt.creditscoring.controller.customer.CustomerCreditReportController.FETCH_USER_REPORT_V1_ENDPOINT;
import static com.yolt.creditscoring.service.clienttoken.model.ClientTokenPermission.DOWNLOAD_REPORT;
import static com.yolt.creditscoring.service.clienttoken.model.ClientTokenPermission.INVITE_USER;
import static com.yolt.creditscoring.service.securitymodule.jwt.JwtCreationService.TOKEN_EXPIRATION_TIME_MINUTES;
import static com.yolt.creditscoring.service.securitymodule.jwt.JwtCreationService.TOKEN_TYPE_PREFIX;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@IntegrationTest
class CustomerCreditReportControllerIT {
    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private ClientAdminRepository clientAdminRepository;

    @Autowired
    private CreditScoreUserRepository creditScoreUserRepository;

    @Autowired
    private CreditScoreReportRepository creditScoreReportRepository;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JwtCreationService jwtCreationService;

    @Autowired
    private UserJourneyRepository userJourneyRepository;

    @Autowired
    private RecurringTransactionsMonthlyReportRepository cycleTransactionsMonthlyReportRepository;

    @Autowired
    private ClientTokenRepository clientTokenRepository;

    @Autowired
    private EstimateRepository estimateRepository;

    @Autowired
    private JwtEncryption jwtEncryption;

    @Autowired
    private PublicKeyRepository publicKeyRepository;

    @MockBean
    private AdminAuditService adminAuditService;

    @MockBean
    private SemaEventService semaEventService;

    @SpyBean
    VaultSecretKeyService vaultSecretKeyService;

    private static final UUID SOME_CLIENT_ID_3 = UUID.fromString("f1fce845-024a-4f0f-bc54-4c03db81bf31");
    private static final UUID SOME_CLIENT_3_ADMIN_ID = UUID.fromString("084c1caf-a731-495f-a362-22b3a788fb5c");
    private static final String SOME_CLIENT_3_ADMIN_EMAIL = "admin3@example.com";
    private static final String SOME_CLIENT_3_ADMIN_IDP_ID = "25bf90e7-a0f5-4b8e-bc09-b0fce6f42abb";
    private static final String SOME_CLIENT_3_NAME = "Some Test Client 3";
    private static final ClientAdmin CLIENT_ADMIN_3 = new ClientAdmin();
    static {
        CLIENT_ADMIN_3.setId(SOME_CLIENT_3_ADMIN_ID);
        CLIENT_ADMIN_3.setClientId(SOME_CLIENT_ID_3);
        CLIENT_ADMIN_3.setEmail(SOME_CLIENT_3_ADMIN_EMAIL);
        CLIENT_ADMIN_3.setIdpId(SOME_CLIENT_3_ADMIN_IDP_ID);
        CLIENT_ADMIN_3.setAuthProvider(AuthProvider.GITHUB);
    }

    @AfterEach
    void afterTest() {
        clientTokenRepository.deleteAll();
        userJourneyRepository.deleteAll();
        creditScoreReportRepository.deleteAll();
        cycleTransactionsMonthlyReportRepository.deleteAll();
        estimateRepository.deleteAll();
        creditScoreUserRepository.deleteAll();
        clientTokenRepository.deleteAll();
    }

    @Test
    void shouldFetchFullUserReport() throws Exception {
        // Given
        ClientEntity client = createClientEntityClient3();
        clientRepository.save(client);
        clientAdminRepository.save(CLIENT_ADMIN_3);

        prepareCreditScoreUsers();
        prepareCreditScoreMonths();

        estimateRepository.save(new EstimateEntity()
                .setId(UUID.randomUUID())
                .setUserId(SOME_USER_ID)
                .setGrade(RiskClassification.B)
                .setScore(10)
                .setStatus(PdStatus.COMPLETED));

        // When
        ResultActions perform = mvc.perform(get(FETCH_USER_REPORT_V1_ENDPOINT, SOME_USER_ID)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + createServerToServerToken(DOWNLOAD_REPORT, CLIENT_ADMIN_3)));

        // Then
        hasSecurityHeaderSetup(perform)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.userInvitationStatus").value("COMPLETED"))
                .andExpect(jsonPath("$", aMapWithSize(6)));

        thenOverviewValuesOk(perform);
        thenCategoriesValuesOk(perform);
        thenMonthlyValuesOk(perform);
        thenRiskClassificationValuesOk(perform);

        then(adminAuditService).should(times(1))
                .adminFetchCreditReport(eq(SOME_CLIENT_ID_3), any(), eq(SOME_CLIENT_3_ADMIN_EMAIL), eq(SOME_USER_ID));

        then(semaEventService).should(never())
                .logClientTokenAccessToUnauthorizedEndpoint(any(), any(), any());
    }

    @Test
    void shouldFetchFullUserReportWhenJWTSigningKeyRotate() throws Exception {
        // Given
        ClientEntity client = createClientEntityClient3();
        clientRepository.save(client);
        clientAdminRepository.save(CLIENT_ADMIN_3);

        prepareCreditScoreUsers();
        prepareCreditScoreMonths();

        estimateRepository.save(new EstimateEntity()
                .setId(UUID.randomUUID())
                .setUserId(SOME_USER_ID)
                .setGrade(RiskClassification.B)
                .setScore(10)
                .setStatus(PdStatus.COMPLETED));

        String oldServerToServerToken = createServerToServerToken(DOWNLOAD_REPORT, CLIENT_ADMIN_3);

        RsaJsonWebKey rsaJsonWebKeyNew = RsaJwkGenerator.generateJwk(2048);
        UUID signingKeyIdNew = UUID.fromString("6196257e-58f6-4e29-8d7f-31e00867b214");
        rsaJsonWebKeyNew.setKeyId(signingKeyIdNew.toString());
        given(vaultSecretKeyService.getJwtSignKeyId()).willReturn(signingKeyIdNew);
        given(vaultSecretKeyService.getJwtSigningPublicKey()).willReturn(rsaJsonWebKeyNew.getRsaPublicKey());

        // When
        ResultActions perform = mvc.perform(get(FETCH_USER_REPORT_V1_ENDPOINT, SOME_USER_ID)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + oldServerToServerToken));

        // Then
        hasSecurityHeaderSetup(perform)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.userInvitationStatus").value("COMPLETED"))
                .andExpect(jsonPath("$", aMapWithSize(6)));

        thenOverviewValuesOk(perform);
        thenCategoriesValuesOk(perform);
        thenMonthlyValuesOk(perform);
        thenRiskClassificationValuesOk(perform);

        then(adminAuditService).should(times(1))
                .adminFetchCreditReport(eq(SOME_CLIENT_ID_3), any(), eq(SOME_CLIENT_3_ADMIN_EMAIL), eq(SOME_USER_ID));

        then(semaEventService).should(never())
                .logClientTokenAccessToUnauthorizedEndpoint(any(), any(), any());
    }

    /**
     * Feature toggle was switch on after Estimate report was calculated
     */
    @Test
    void shouldFetchUserReportWithoutEstimate() throws Exception {
        // Given
        ClientEntity client = createClientEntityClient3();
        clientRepository.save(client);
        clientAdminRepository.save(CLIENT_ADMIN_3);

        prepareCreditScoreUsers();
        prepareCreditScoreMonths();

        // When
        ResultActions perform = mvc.perform(get(FETCH_USER_REPORT_V1_ENDPOINT, SOME_USER_ID)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + createServerToServerToken(DOWNLOAD_REPORT, CLIENT_ADMIN_3)));

        // Then
        hasSecurityHeaderSetup(perform)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$", aMapWithSize(6)))
                .andExpect(jsonPath("$.userInvitationStatus").value("COMPLETED"))
                .andExpect(jsonPath("$.riskClassification").isEmpty());

        thenOverviewValuesOk(perform);
        thenCategoriesValuesOk(perform);
        thenMonthlyValuesOk(perform);


        then(adminAuditService).should(times(1))
                .adminFetchCreditReport(eq(SOME_CLIENT_ID_3), any(), eq(SOME_CLIENT_3_ADMIN_EMAIL), eq(SOME_USER_ID));

        then(semaEventService).should(never())
                .logClientTokenAccessToUnauthorizedEndpoint(any(), any(), any());
    }

    @Test
    void shouldFetchUserReportWithNotEnoughTransactionPDStatus() throws Exception {
        // Given
        ClientEntity client = createClientEntityClient3();
        clientRepository.save(client);
        clientAdminRepository.save(CLIENT_ADMIN_3);

        prepareCreditScoreUsers();
        prepareCreditScoreMonths();

        estimateRepository.save(new EstimateEntity()
                .setId(UUID.randomUUID())
                .setUserId(SOME_USER_ID)
                .setGrade(null)
                .setScore(null)
                .setStatus(PdStatus.ERROR_NOT_ENOUGH_TRANSACTIONS));

        // When
        ResultActions perform = mvc.perform(get(FETCH_USER_REPORT_V1_ENDPOINT, SOME_USER_ID)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + createServerToServerToken(ClientTokenPermission.DOWNLOAD_REPORT, CLIENT_ADMIN_3)));

        // Then
        hasSecurityHeaderSetup(perform)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.userInvitationStatus").value("COMPLETED"))
                .andExpect(jsonPath("$", aMapWithSize(6)));

        thenOverviewValuesOk(perform);
        thenCategoriesValuesOk(perform);
        thenMonthlyValuesOk(perform);
        thenRiskClassificationValuesAtNotEnoughTransactions(perform);

        then(adminAuditService).should(times(1))
                .adminFetchCreditReport(eq(SOME_CLIENT_ID_3), any(), eq(SOME_CLIENT_3_ADMIN_EMAIL), eq(SOME_USER_ID));
    }

    @Test
    void shouldFetchUserReportWithEstimateFeatureToggleOff() throws Exception {
        // Given
        ClientEntity client = createClientEntityClient3();
        client.setPDScoreFeatureToggle(false);
        clientRepository.save(client);
        clientAdminRepository.save(CLIENT_ADMIN_3);

        prepareCreditScoreUsers();
        prepareCreditScoreMonths();

        // When
        ResultActions perform = mvc.perform(get(FETCH_USER_REPORT_V1_ENDPOINT, SOME_USER_ID)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + createServerToServerToken(DOWNLOAD_REPORT, CLIENT_ADMIN_3)));

        // Then
        hasSecurityHeaderSetup(perform)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$", aMapWithSize(6)))
                .andExpect(jsonPath("$.userInvitationStatus").value("COMPLETED"))
                .andExpect(jsonPath("$.riskClassification").isEmpty());

        thenOverviewValuesOk(perform);
        thenCategoriesValuesOk(perform);
        thenMonthlyValuesOk(perform);

        then(adminAuditService).should(times(1))
                .adminFetchCreditReport(eq(SOME_CLIENT_ID_3), any(), eq(SOME_CLIENT_3_ADMIN_EMAIL), eq(SOME_USER_ID));

        then(semaEventService).should(never())
                .logClientTokenAccessToUnauthorizedEndpoint(any(), any(), any());
    }

    @Test
    void shouldFetchUserReportWithEstimateFeatureToggleOffAndCategoryFeatureToggleOff() throws Exception {
        // Given
        ClientEntity client = createClientEntityClient3();
        client.setPDScoreFeatureToggle(false);
        client.setCategoryFeatureToggle(false);
        clientRepository.save(client);
        clientAdminRepository.save(CLIENT_ADMIN_3);

        prepareCreditScoreUsers();
        prepareCreditScoreMonths();

        // When
        ResultActions perform = mvc.perform(get(FETCH_USER_REPORT_V1_ENDPOINT, SOME_USER_ID)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + createServerToServerToken(DOWNLOAD_REPORT, CLIENT_ADMIN_3)));

        // Then
        hasSecurityHeaderSetup(perform)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$", aMapWithSize(6)))
                .andExpect(jsonPath("$.categories").isEmpty())
                .andExpect(jsonPath("$.userInvitationStatus").value("COMPLETED"))
                .andExpect(jsonPath("$.riskClassification").isEmpty());

        thenOverviewValuesOk(perform);
        thenMonthlyValuesOk(perform);

        then(adminAuditService).should(times(1))
                .adminFetchCreditReport(eq(SOME_CLIENT_ID_3), any(), eq(SOME_CLIENT_3_ADMIN_EMAIL), eq(SOME_USER_ID));

        then(semaEventService).should(never())
                .logClientTokenAccessToUnauthorizedEndpoint(any(), any(), any());
    }

    @Test
    void shouldFetchUserReportWithEstimateFeatureToggleOffAndCategoryFeatureToggleOffAndMonthsFeatureToggleOff() throws Exception {
        // Given
        ClientEntity client = createClientEntityClient3();
        client.setPDScoreFeatureToggle(false);
        client.setCategoryFeatureToggle(false);
        client.setMonthsFeatureToggle(false);
        clientRepository.save(client);
        clientAdminRepository.save(CLIENT_ADMIN_3);

        prepareCreditScoreUsers();
        prepareCreditScoreMonths();

        // When
        ResultActions perform = mvc.perform(get(FETCH_USER_REPORT_V1_ENDPOINT, SOME_USER_ID)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + createServerToServerToken(DOWNLOAD_REPORT, CLIENT_ADMIN_3)));

        // Then
        hasSecurityHeaderSetup(perform)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$", aMapWithSize(6)))
                .andExpect(jsonPath("$.categories").isEmpty())
                .andExpect(jsonPath("$.months").isEmpty())
                .andExpect(jsonPath("$.riskClassification").isEmpty())
                .andExpect(jsonPath("$.userInvitationStatus").value("COMPLETED"));

        thenOverviewValuesOk(perform);

        then(adminAuditService).should(times(1))
                .adminFetchCreditReport(eq(SOME_CLIENT_ID_3), any(), eq(SOME_CLIENT_3_ADMIN_EMAIL), eq(SOME_USER_ID));

        then(semaEventService).should(never())
                .logClientTokenAccessToUnauthorizedEndpoint(any(), any(), any());
    }

    @Test
    void shouldFetchUserReportWithAllFeatureToggleOff() throws Exception {
        // Given
        ClientEntity client = createClientEntityClient3();
        client.setPDScoreFeatureToggle(false);
        client.setCategoryFeatureToggle(false);
        client.setMonthsFeatureToggle(false);
        client.setOverviewFeatureToggle(false);
        clientRepository.save(client);
        clientAdminRepository.save(CLIENT_ADMIN_3);


        prepareCreditScoreUsers();
        prepareCreditScoreMonths();

        // When
        ResultActions perform = mvc.perform(get(FETCH_USER_REPORT_V1_ENDPOINT, SOME_USER_ID)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + createServerToServerToken(DOWNLOAD_REPORT, CLIENT_ADMIN_3)));

        // Then
        hasSecurityHeaderSetup(perform)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$", aMapWithSize(6)))
                .andExpect(jsonPath("$.overview").isEmpty())
                .andExpect(jsonPath("$.categories").isEmpty())
                .andExpect(jsonPath("$.months").isEmpty())
                .andExpect(jsonPath("$.userInvitationStatus").value("COMPLETED"))
                .andExpect(jsonPath("$.riskClassification").isEmpty());

        then(adminAuditService).should(times(1))
                .adminFetchCreditReport(eq(SOME_CLIENT_ID_3), any(), eq(SOME_CLIENT_3_ADMIN_EMAIL), eq(SOME_USER_ID));

        then(semaEventService).should(never())
                .logClientTokenAccessToUnauthorizedEndpoint(any(), any(), any());
    }

    @Test
    void shouldNotFetchUserReportWithInviteToken() throws Exception {
        // Given
        ClientEntity client = createClientEntityClient3();
        clientRepository.save(client);
        clientAdminRepository.save(CLIENT_ADMIN_3);

        prepareCreditScoreUsers();
        prepareCreditScoreMonths();

        // When
        ResultActions perform = mvc.perform(get(FETCH_USER_REPORT_V1_ENDPOINT, SOME_USER_ID)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + createServerToServerToken(INVITE_USER, CLIENT_ADMIN_3)));

        // Then
        hasSecurityHeaderSetup(perform)
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorType", equalTo("UNKNOWN")));

        then(adminAuditService).should(never())
                .adminFetchCreditReport(any(), any(), any(), any());

        then(semaEventService).should()
                .logClientTokenAccessToUnauthorizedEndpoint(
                        any(),
                        eq("/api/customer/v1/users/ba459bdb-5032-43ff-a339-500e9b20cf26/report"),
                        argThat(list -> list.containsAll(List.of(new SimpleGrantedAuthority(INVITE_USER.name()), new SimpleGrantedAuthority(SecurityRoles.ROLE_PREFIX + SecurityRoles.CLIENT_TOKEN)))));
    }

    @Test
    void shouldNotFetchUserReportWithValidClientAdminToken() throws Exception {
        // Given
        ClientEntity client = createClientEntityClient3();
        clientRepository.save(client);
        clientAdminRepository.save(CLIENT_ADMIN_3);

        prepareCreditScoreUsers();
        prepareCreditScoreMonths();

        // When
        ResultActions perform = mvc.perform(get(FETCH_USER_REPORT_V1_ENDPOINT, SOME_USER_ID)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtCreationService.createAdminToken(createOauth2AdminUser(CLIENT_ADMIN_3))))
        ;

        // Then
        hasSecurityHeaderSetup(perform)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorType", equalTo("TOKEN_INVALID")));

        then(adminAuditService).should(never())
                .adminFetchCreditReport(any(), any(), any(), any());

        then(semaEventService).should(never())
                .logClientTokenAccessToUnauthorizedEndpoint(any(), any(), any());
    }

    @Test
    void shouldNotFetchReportForNotExistingUser() throws Exception {
        // Given
        ClientEntity client = createClientEntityClient3();
        clientRepository.save(client);
        clientAdminRepository.save(CLIENT_ADMIN_3);

        prepareCreditScoreUsers();
        prepareCreditScoreMonths();


        // When
        String fakeId = "347f7d89-575d-4a01-8562-f43868072f77";
        ResultActions perform = mvc.perform(get(FETCH_USER_REPORT_V1_ENDPOINT, fakeId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + createServerToServerToken(DOWNLOAD_REPORT, CLIENT_ADMIN_3)));

        hasSecurityHeaderSetup(perform)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorType", equalTo("USER_NOT_FOUND")));

        then(adminAuditService).should(never())
                .adminFetchCreditReport(any(), any(), any(), any());

        then(semaEventService).should(never())
                .logClientTokenAccessToUnauthorizedEndpoint(any(), any(), any());
    }

    @Test
    void shouldNotFetchReportForUserBelongingToOtherClient() throws Exception {
        // Given
        ClientEntity client = createClientEntityClient3();
        clientRepository.save(client);
        clientAdminRepository.save(CLIENT_ADMIN_3);

        prepareCreditScoreUsers();
        prepareCreditScoreMonths();

        // When
        ResultActions perform = mvc.perform(get(FETCH_USER_REPORT_V1_ENDPOINT, SOME_USER_ID)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " +
                        createServerToServerToken(DOWNLOAD_REPORT, new ClientAdmin(SOME_CLIENT_ID, SOME_CLIENT_ADMIN_EMAIL, SOME_CLIENT_ID, SOME_CLIENT_ADMIN_IDP_ID, AuthProvider.GOOGLE))));

        hasSecurityHeaderSetup(perform)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorType", equalTo("USER_NOT_FOUND")));

        then(adminAuditService).should(never())
                .adminFetchCreditReport(any(), any(), any(), any());

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
    void shouldNotFetchReportUserWithValidTokenSignedByOtherAuthority() throws Exception {
        // Given
        RsaJsonWebKey rsaJsonWebKey = RsaJwkGenerator.generateJwk(2048);
        UUID signingKeyId = UUID.randomUUID();
        rsaJsonWebKey.setKeyId(signingKeyId.toString());

        final PublicKeyEntity publicKeyEntity = new PublicKeyEntity();
        publicKeyEntity.setKid(UUID.fromString(rsaJsonWebKey.getKeyId()));
        publicKeyEntity.setCreatedDate(OffsetDateTime.now());
        publicKeyEntity.setPublicKey(rsaJsonWebKey.getRsaPublicKey().getEncoded());

        publicKeyRepository.save(publicKeyEntity);

        var clientTokenEntity = ClientTokenEntity.builder()
                .jwtId(UUID.randomUUID())
                .clientId(SOME_CLIENT_ID)
                .signedPublicKeyId(signingKeyId)
                .name("Testing token")
                .permissions(List.of(DOWNLOAD_REPORT))
                .expirationDate(OffsetDateTime.now().plusDays(10))
                .createdDate(OffsetDateTime.now())
                .createdAdminEmail(SOME_CLIENT_ADMIN_EMAIL)
                .status(ClientTokenStatus.ACTIVE)
                .build();
        clientTokenRepository.save(clientTokenEntity);

        JwtClaims claims = new JwtClaims();
        claims.setSubject(SOME_CLIENT_ADMIN_IDP_ID);
        claims.setIssuedAtToNow();
        claims.setJwtId(clientTokenEntity.getJwtId().toString());
        claims.setExpirationTimeMinutesInTheFuture(TOKEN_EXPIRATION_TIME_MINUTES);
        claims.setClaim("scope", List.of(DOWNLOAD_REPORT));

        JsonWebSignature jws = new JsonWebSignature();
        jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.RSA_USING_SHA512);
        jws.setPayload(claims.toJson());

        RsaJsonWebKey rsaJsonWebKeyFake = RsaJwkGenerator.generateJwk(2048);
        rsaJsonWebKeyFake.setKeyId(signingKeyId.toString());

        jws.setKey(rsaJsonWebKeyFake.getRsaPrivateKey());

        var token = TOKEN_TYPE_PREFIX + " " + jwtEncryption.encrypt(jws.getCompactSerialization());

        // When
        ResultActions perform = mvc.perform(get(FETCH_USER_REPORT_V1_ENDPOINT, SOME_USER_ID)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON));
        // Then
        perform
                .andExpect(status().isUnauthorized());

        Iterable<CreditScoreUser> users = creditScoreUserRepository.findAll();
        assertThat(users).isEmpty();

        then(adminAuditService).should(never())
                .inviteNewUser(any(), any(), any(), any(), any(), any(), any());

        then(semaEventService).should(never())
                .logClientTokenAccessToUnauthorizedEndpoint(any(), any(), any());
    }

    private void prepareCreditScoreUsers() {


        CreditScoreUser user1 = new CreditScoreUser()
                .setId(SOME_USER_ID)
                .setName(SOME_USER_NAME)
                .setEmail(SOME_USER_EMAIL)
                .setDateTimeInvited(SOME_FIXED_TEST_DATE)
                .setDateTimeStatusChange(SOME_FIXED_TEST_DATE)
                .setStatus(InvitationStatus.INVITED)
                .setInvitationHash(SOME_USER_HASH)
                .setClientId(SOME_CLIENT_ID_3)
                .setYoltUserId(SOME_YOLT_USER_ID)
                .setAdminEmail(SOME_CLIENT_ADMIN_2_EMAIL);

        List<String> signatureJsonPaths = List.of(
                "$['userId']",
                "$['iban']",
                "$['initialBalance']",
                "$['currency']",
                "$['newestTransactionDate']",
                "$['oldestTransactionDate']",
                "$['transactionsSize']"
        );

        CreditScoreReport creditScoreReport = CreditScoreReport.builder()
                .id(SOME_CREDIT_REPORT_ID)
                .accountReference(AccountReference.builder()
                        .iban("NL79ABNA12345678901")
                        .bban("79ABNA12345678901")
                        .sortCodeAccountNumber("9455762838")
                        .maskedPan("1234 **** **** 5678")
                        .build())
                .initialBalance(new BigDecimal("5000.00"))
                .lastDataFetchTime(OffsetDateTime.of(2021, 1, 1, 10, 0, 0, 0, ZoneOffset.UTC))
                .currency("EUR")
                .transactionsSize(100)
                .creditLimit(new BigDecimal("-1000.00"))
                .newestTransactionDate(LocalDate.of(2020, 12, 31))
                .oldestTransactionDate(LocalDate.of(2020, 12, 1))
                .accountHolder("Account Holder")
                .creditScoreUserId(user1.getId())
                .signatureKeyId(SOME_REPORT_SIGNATURE_KEY_ID)
                .signatureJsonPaths(signatureJsonPaths)
                .build();

        CreditScoreMonthlyReport creditScoreMonthlyReport202101 = CreditScoreMonthlyReport.builder()
                .id(UUID.randomUUID())
                .year(2021)
                .month(1)
                .highestBalance(new BigDecimal("2000.02"))
                .lowestBalance(new BigDecimal("30000.03"))
                .averageBalance(new BigDecimal("4000.04"))
                .categorizedAmount(Category.OTHER_INCOME, new BigDecimal("1000.01"), 1)
                .categorizedAmount(Category.OTHER_EXPENSES, new BigDecimal("5000.05"), 2)
                .incomingTransactionsSize(2)
                .outgoingTransactionsSize(1)
                .build();

        CreditScoreMonthlyReport creditScoreMonthlyReport202012 = CreditScoreMonthlyReport.builder()
                .id(UUID.randomUUID())
                .year(2020)
                .month(12)
                .highestBalance(new BigDecimal("15000.00"))
                .lowestBalance(new BigDecimal("10000.00"))
                .averageBalance(new BigDecimal("12000.00"))
                .categorizedAmount(Category.OTHER_INCOME, new BigDecimal("10000.00"), 1)
                .categorizedAmount(Category.OTHER_EXPENSES, new BigDecimal("5000.00"), 2)
                .incomingTransactionsSize(6)
                .outgoingTransactionsSize(4)
                .build();

        CreditScoreMonthlyReport creditScoreMonthlyReport202011 = CreditScoreMonthlyReport.builder()
                .id(UUID.randomUUID())
                .year(2020)
                .month(11)
                .highestBalance(new BigDecimal("10000.00"))
                .lowestBalance(new BigDecimal("5000.00"))
                .averageBalance(new BigDecimal("6000.00"))
                .categorizedAmount(Category.REVENUE, new BigDecimal("10000.00"), 1)
                .categorizedAmount(Category.OTHER_INCOME, new BigDecimal("1000.00"), 2)
                .categorizedAmount(Category.SALES_TAX, new BigDecimal("1000.00"), 3)
                .categorizedAmount(Category.OTHER_EXPENSES, new BigDecimal("5000.00"), 4)
                .incomingTransactionsSize(6)
                .outgoingTransactionsSize(4)
                .build();

        CreditScoreMonthlyReport creditScoreMonthlyReport201912 = CreditScoreMonthlyReport.builder()
                .id(UUID.randomUUID())
                .year(2019)
                .month(12)
                .highestBalance(new BigDecimal("500.00"))
                .lowestBalance(new BigDecimal("300.00"))
                .averageBalance(new BigDecimal("600.00"))
                .categorizedAmount(Category.REVENUE, new BigDecimal("10000.00"), 1)
                .categorizedAmount(Category.OTHER_INCOME, new BigDecimal("1000.00"), 2)
                .categorizedAmount(Category.SALES_TAX, new BigDecimal("1000.00"), 3)
                .categorizedAmount(Category.OTHER_EXPENSES, new BigDecimal("5000.00"), 4)
                .categorizedAmount(Category.TAX_RETURNS, new BigDecimal("20.00"), 2)
                .incomingTransactionsSize(7)
                .outgoingTransactionsSize(5)
                .build();

        creditScoreMonthlyReport202101.setCreditScoreReport(creditScoreReport);
        creditScoreMonthlyReport202012.setCreditScoreReport(creditScoreReport);
        creditScoreMonthlyReport202011.setCreditScoreReport(creditScoreReport);
        creditScoreMonthlyReport201912.setCreditScoreReport(creditScoreReport);

        creditScoreReport.setCreditScoreMonthly(Set.of(creditScoreMonthlyReport202101, creditScoreMonthlyReport202012, creditScoreMonthlyReport202011, creditScoreMonthlyReport201912));
        creditScoreReport.setSignature("Kxf6gtjpDwubibfnqaTEYavr/yE9XAEY7FmtUCZLmQyIzlqKJS7GSV6nzOCbsZxsNzpOhTQIrmeDIq+9HwBb90qqy/6jHX3yy8WtO93vScjyC6/H95Taf2h1+lHWyEhEU+jRB4glg5PuTREToPcYRmVUDIyLtI7s6WF7SR1nHjL8t0eDRwVTRQE8O5ndzdwIKAgIUpdzitNsnSIfkGHKZwlABcB/bRdJ/tsnpIq+zDD6g0qFjD3x1esAPWvtzwGOEOf4hJc53GyHet1UE2gXQmdGzGti0mt5mRXQq8eVJn/hip2tHfYnDq6v1x0LnOcH3izSTIl7Nkvp9YCmpXjejytInEI/wN/G5s6UDstUUs0U0l3w7Ixa0jQVoVfYWBPt5e8Jn0ru3OPiYyQYA/+cVqU3OrpvCnpHSJaGlysaCn8SOTH6YFKV49xz+J2flHm2Xx+TwHTSCdmkR2SWfLyRjRmvPduhatv1p++4R9lD29Nc4mm0r4/dWmh7WNd5bGh2FHJiup3GcEcLx77Uc1mk5mOjN7CHbU4d78rFgf6YLCcjqh+N0FQAv67BPIlrTfbN6kP41BhBU4iSKkVlYwzBOOJAw45kfxPt3KBXnBRWAdXol91SF2RATw5YU7tmoypcMOxXZQBNRbwXLEJjUZd7bevDodpvaW8AjKKH1uMY7aI=");
        creditScoreReport.setSignatureKeyId(SOME_REPORT_SIGNATURE_KEY_ID);

        CreditScoreUser user2 = new CreditScoreUser()
                .setId(SOME_USER_ID_2)
                .setName(SOME_USER_NAME_2)
                .setEmail(SOME_USER_EMAIL_2)
                .setDateTimeInvited(SOME_FIXED_TEST_DATE_2)
                .setDateTimeStatusChange(SOME_FIXED_TEST_DATE_2)
                .setStatus(InvitationStatus.EXPIRED)
                .setInvitationHash(SOME_USER_HASH_2)
                .setClientId(SOME_CLIENT_ID)
                .setClientEmailId(SOME_CLIENT_EMAIL_ID)
                .setAdminEmail(SOME_CLIENT_ADMIN_EMAIL);

        CreditScoreUser user3 = new CreditScoreUser()
                .setId(SOME_USER_ID_3)
                .setName(SOME_USER_NAME)
                .setEmail(SOME_USER_EMAIL)
                .setDateTimeInvited(SOME_FIXED_TEST_DATE)
                .setDateTimeStatusChange(SOME_FIXED_TEST_DATE)
                .setStatus(InvitationStatus.INVITED)
                .setInvitationHash(SOME_USER_HASH_3)
                .setClientId(SOME_CLIENT_ID_3)
                .setYoltUserId(SOME_YOLT_USER_ID)
                .setAdminEmail(SOME_CLIENT_ADMIN_EMAIL);

        CreditScoreReport creditScoreReport3 = CreditScoreReport.builder()
                .id(UUID.randomUUID())
                .accountReference(AccountReference.builder().iban("NL79ABNA12345678901").build())
                .initialBalance(new BigDecimal("5000.00"))
                .currency("EUR")
                .transactionsSize(100)
                .creditLimit(new BigDecimal("-1000.00"))
                .newestTransactionDate(LocalDate.of(2020, 12, 31))
                .oldestTransactionDate(LocalDate.of(2020, 12, 1))
                .creditScoreUserId(user3.getId())
                .signatureKeyId(SOME_REPORT_SIGNATURE_KEY_ID)
                .signatureJsonPaths(signatureJsonPaths)
                .build();

        CreditScoreMonthlyReport creditScoreMonthlyReport3 = CreditScoreMonthlyReport.builder()
                .id(UUID.randomUUID())
                .year(2020)
                .month(12)
                .highestBalance(new BigDecimal("15000.00"))
                .lowestBalance(new BigDecimal("10000.00"))
                .averageBalance(new BigDecimal("12000.00"))
                .incomingTransactionsSize(6)
                .outgoingTransactionsSize(4)
                .build();

        creditScoreMonthlyReport3.setCreditScoreReport(creditScoreReport3);
        creditScoreReport3.setCreditScoreMonthly(Collections.singleton(creditScoreMonthlyReport3));
        creditScoreReport3.setSignature("Kxf6gtjpDwubibfnqaTEYavr/yE9XAEY7FmtUCZLmQyIzlqKJS7GSV6nzOCbsZxsNzpOhTQIrmeDIq+9HwBb90qqy/6jHX3yy8WtO93vScjyC6/H95Taf2h1+lHWyEhEU+jRB4glg5PuTREToPcYRmVUDIyLtI7s6WF7SR1nHjL8t0eDRwVTRQE8O5ndzdwIKAgIUpdzitNsnSIfkGHKZwlABcB/bRdJ/tsnpIq+zDD6g0qFjD3x1esAPWvtzwGOEOf4hJc53GyHet1UE2gXQmdGzGti0mt5mRXQq8eVJn/hip2tHfYnDq6v1x0LnOcH3izSTIl7Nkvp9YCmpXjejytInEI/wN/G5s6UDstUUs0U0l3w7Ixa0jQVoVfYWBPt5e8Jn0ru3OPiYyQYA/+cVqU3OrpvCnpHSJaGlysaCn8SOTH6YFKV49xz+J2flHm2Xx+TwHTSCdmkR2SWfLyRjRmvPduhatv1p++4R9lD29Nc4mm0r4/dWmh7WNd5bGh2FHJiup3GcEcLx77Uc1mk5mOjN7CHbU4d78rFgf6YLCcjqh+N0FQAv67BPIlrTfbN6kP41BhBU4iSKkVlYwzBOOJAw45kfxPt3KBXnBRWAdXol91SF2RATw5YU7tmoypcMOxXZQBNRbwXLEJjUZd7bevDodpvaW8AjKKH1uMY7aI=");
        creditScoreReport3.setSignatureKeyId(SOME_REPORT_SIGNATURE_KEY_ID);

        creditScoreUserRepository.saveAll(Arrays.asList(user1, user2, user3));
        creditScoreReportRepository.save(creditScoreReport);
        creditScoreReportRepository.save(creditScoreReport3);
    }

    private void prepareCreditScoreMonths() {
        cycleTransactionsMonthlyReportRepository.save(RecurringTransactionsMonthlyReportEntity.builder()
                .id(UUID.randomUUID())
                .creditScoreId(SOME_CREDIT_REPORT_ID)
                .year(2020)
                .month(12)
                .incomeRecurringAmount(new BigDecimal("0"))
                .incomeRecurringSize(0)
                .outcomeRecurringAmount(new BigDecimal("69.97"))
                .outcomeRecurringSize(1)
                .build());
        cycleTransactionsMonthlyReportRepository.save(RecurringTransactionsMonthlyReportEntity.builder()
                .id(UUID.randomUUID())
                .creditScoreId(SOME_CREDIT_REPORT_ID)
                .year(2020)
                .month(11)
                .incomeRecurringAmount(new BigDecimal("149.17"))
                .incomeRecurringSize(1)
                .outcomeRecurringAmount(new BigDecimal("0"))
                .outcomeRecurringSize(0)
                .build());
    }

    private String createServerToServerToken(ClientTokenPermission permission, ClientAdmin clientAdmin) throws Exception {
        ResultActions performToken = mvc.perform(post(CREATE_TOKEN_ENDPOINT)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtCreationService.createAdminToken(createOauth2AdminUser(clientAdmin)))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        String.format("""
                                            {
                                                "name": "%s",
                                                "permissions": ["%s"]
                                            }
                                        """,
                                SOME_CLIENT_JWT_NAME,
                                permission)
                )
        );

        ObjectMapper mapper = new ObjectMapper();
        String contentAsString = performToken.andReturn().getResponse().getContentAsString();
        ClientTokenController.ClientTokenResponse clientTokenResponse =
                mapper.readValue(contentAsString, ClientTokenController.ClientTokenResponse.class);
        return clientTokenResponse.clientToken();
    }

    private void thenRiskClassificationValuesOk(ResultActions perform) throws Exception {
        perform.andExpect(jsonPath("$.riskClassification", aMapWithSize(4)))
                .andExpect(jsonPath("$.riskClassification.rateLower").value("0.5"))
                .andExpect(jsonPath("$.riskClassification.rateUpper").value("1.5"))
                .andExpect(jsonPath("$.riskClassification.grade").value("B"))
                .andExpect(jsonPath("$.riskClassification.status").value("COMPLETED"));
    }

    private void thenRiskClassificationValuesAtNotEnoughTransactions(ResultActions perform) throws Exception {
        perform.andExpect(jsonPath("$.riskClassification", aMapWithSize(4)))
                .andExpect(jsonPath("$.riskClassification.rateLower").isEmpty())
                .andExpect(jsonPath("$.riskClassification.rateUpper").isEmpty())
                .andExpect(jsonPath("$.riskClassification.grade").isEmpty())
                .andExpect(jsonPath("$.riskClassification.status").value("ERROR_NOT_ENOUGH_TRANSACTIONS"));
    }

    private static void thenOverviewValuesOk(ResultActions perform) throws Exception {
        perform.andExpect(jsonPath("$.accountDetails", aMapWithSize(13)))
                .andExpect(jsonPath("$.accountDetails.userId").value("ba459bdb-5032-43ff-a339-500e9b20cf26"))
                .andExpect(jsonPath("$.accountDetails.iban").value("NL79ABNA12345678901"))
                .andExpect(jsonPath("$.accountDetails.bban").value("79ABNA12345678901"))
                .andExpect(jsonPath("$.accountDetails.maskedPan").value("1234 **** **** 5678"))
                .andExpect(jsonPath("$.accountDetails.sortCodeAccountNumber").value("9455762838"))
                .andExpect(jsonPath("$.accountDetails.initialBalance").value("5000.00"))
                .andExpect(jsonPath("$.accountDetails.lastDataFetchTime").value("2021-01-01T10:00:00Z"))
                .andExpect(jsonPath("$.accountDetails.currency").value("EUR"))
                .andExpect(jsonPath("$.accountDetails.newestTransactionDate").value("2020-12-31"))
                .andExpect(jsonPath("$.accountDetails.oldestTransactionDate").value("2020-12-01"))
                .andExpect(jsonPath("$.accountDetails.creditLimit").value("-1000.00"))
                .andExpect(jsonPath("$.accountDetails.transactionsSize").value("100"))
                .andExpect(jsonPath("$.accountDetails.accountHolder").value("Account Holder"))
                .andExpect(jsonPath("$.overview", aMapWithSize(16)))
                .andExpect(jsonPath("$.overview.averageRecurringIncome").value("12.43"))
                .andExpect(jsonPath("$.overview.averageRecurringCosts").value("5.83"))
                .andExpect(jsonPath("$.overview.startDate").value("2020-01-01"))
                .andExpect(jsonPath("$.overview.endDate").value("2020-12-31"))
                .andExpect(jsonPath("$.overview.incomingTransactionsSize").value(12))
                .andExpect(jsonPath("$.overview.outgoingTransactionsSize").value(8))
                .andExpect(jsonPath("$.overview.monthlyAverageIncome").value("1750.00"))
                .andExpect(jsonPath("$.overview.monthlyAverageCost").value("916.67"))
                .andExpect(jsonPath("$.overview.totalIncomeAmount").value("21000.00"))
                .andExpect(jsonPath("$.overview.totalOutgoingAmount").value("11000.00"))
                .andExpect(jsonPath("$.overview.averageIncomeTransactionAmount").value("1750.00"))
                .andExpect(jsonPath("$.overview.averageOutcomeTransactionAmount").value("1375.00"))
                .andExpect(jsonPath("$.overview.vatTotalPayments").value(3))
                .andExpect(jsonPath("$.overview.vatAverage").value("333.33"))
                .andExpect(jsonPath("$.overview.totalCorporateTax").value("0"));
    }

    private static void thenCategoriesValuesOk(ResultActions perform) throws Exception {
        perform.andExpect(jsonPath("$.categories", hasSize(4)))
                .andExpect(jsonPath("$.categories[?(@.categoryName == 'OTHER_EXPENSES')].categoryType", contains(equalTo("OUTGOING"))))
                .andExpect(jsonPath("$.categories[?(@.categoryName == 'OTHER_EXPENSES')].totalTransactions", contains(equalTo(6))))
                .andExpect(jsonPath("$.categories[?(@.categoryName == 'OTHER_EXPENSES')].averageTransactionAmount", contains(equalTo("1666.66"))))
                .andExpect(jsonPath("$.categories[?(@.categoryName == 'OTHER_EXPENSES')].totalTransactionAmount", contains(equalTo("10000.00"))))
                .andExpect(jsonPath("$.categories[?(@.categoryName == 'OTHER_INCOME')].categoryType", contains(equalTo("INCOMING"))))
                .andExpect(jsonPath("$.categories[?(@.categoryName == 'OTHER_INCOME')].totalTransactions", contains(equalTo(3))))
                .andExpect(jsonPath("$.categories[?(@.categoryName == 'OTHER_INCOME')].averageTransactionAmount", contains(equalTo("3666.66"))))
                .andExpect(jsonPath("$.categories[?(@.categoryName == 'OTHER_INCOME')].totalTransactionAmount", contains(equalTo("11000.00"))))
                .andExpect(jsonPath("$.categories[?(@.categoryName == 'REVENUE')].categoryType", contains(equalTo("INCOMING"))))
                .andExpect(jsonPath("$.categories[?(@.categoryName == 'REVENUE')].totalTransactions", contains(equalTo(1))))
                .andExpect(jsonPath("$.categories[?(@.categoryName == 'REVENUE')].averageTransactionAmount", contains(equalTo("10000.00"))))
                .andExpect(jsonPath("$.categories[?(@.categoryName == 'REVENUE')].totalTransactionAmount", contains(equalTo("10000.00"))))
                .andExpect(jsonPath("$.categories[?(@.categoryName == 'SALES_TAX')].categoryType", contains(equalTo("OUTGOING"))))
                .andExpect(jsonPath("$.categories[?(@.categoryName == 'SALES_TAX')].totalTransactions", contains(equalTo(3))))
                .andExpect(jsonPath("$.categories[?(@.categoryName == 'SALES_TAX')].averageTransactionAmount", contains(equalTo("333.33"))))
                .andExpect(jsonPath("$.categories[?(@.categoryName == 'SALES_TAX')].totalTransactionAmount", contains(equalTo("1000.00"))));
    }

    private static void thenMonthlyValuesOk(ResultActions perform) throws Exception {
        perform.andExpect(jsonPath("$.months", hasSize(4)))
                .andExpect(jsonPath("$.months[0]", aMapWithSize(9)))
                .andExpect(jsonPath("$.months[0].year").value(2019))
                .andExpect(jsonPath("$.months[0].month").value(12))
                .andExpect(jsonPath("$.months[0].highestBalance").value("500.00"))
                .andExpect(jsonPath("$.months[0].lowestBalance").value("300.00"))
                .andExpect(jsonPath("$.months[0].averageBalance").value("600.00"))
                .andExpect(jsonPath("$.months[0].incomingTransactionsSize").value(7))
                .andExpect(jsonPath("$.months[0].outgoingTransactionsSize").value(5))
                .andExpect(jsonPath("$.months[0].totalIncoming").value("11020.00"))
                .andExpect(jsonPath("$.months[0].totalOutgoing").value("6000.00"))
                .andExpect(jsonPath("$.months[1]", aMapWithSize(9)))
                .andExpect(jsonPath("$.months[1].year").value(2020))
                .andExpect(jsonPath("$.months[1].month").value(11))
                .andExpect(jsonPath("$.months[1].highestBalance").value("10000.00"))
                .andExpect(jsonPath("$.months[1].lowestBalance").value("5000.00"))
                .andExpect(jsonPath("$.months[1].averageBalance").value("6000.00"))
                .andExpect(jsonPath("$.months[1].incomingTransactionsSize").value(6))
                .andExpect(jsonPath("$.months[1].outgoingTransactionsSize").value(4))
                .andExpect(jsonPath("$.months[1].totalIncoming").value("11000.00"))
                .andExpect(jsonPath("$.months[1].totalOutgoing").value("6000.00"))
                .andExpect(jsonPath("$.months[2]", aMapWithSize(9)))
                .andExpect(jsonPath("$.months[2].year").value(2020))
                .andExpect(jsonPath("$.months[2].month").value(12))
                .andExpect(jsonPath("$.months[2].highestBalance").value("15000.00"))
                .andExpect(jsonPath("$.months[2].lowestBalance").value("10000.00"))
                .andExpect(jsonPath("$.months[2].averageBalance").value("12000.00"))
                .andExpect(jsonPath("$.months[2].incomingTransactionsSize").value(6))
                .andExpect(jsonPath("$.months[2].outgoingTransactionsSize").value(4))
                .andExpect(jsonPath("$.months[2].totalIncoming").value("10000.00"))
                .andExpect(jsonPath("$.months[2].totalOutgoing").value("5000.00"))
                .andExpect(jsonPath("$.months[3]", aMapWithSize(9)))
                .andExpect(jsonPath("$.months[3].year").value(2021))
                .andExpect(jsonPath("$.months[3].month").value(1))
                .andExpect(jsonPath("$.months[3].highestBalance").value("2000.02"))
                .andExpect(jsonPath("$.months[3].lowestBalance").value("30000.03"))
                .andExpect(jsonPath("$.months[3].averageBalance").value("4000.04"))
                .andExpect(jsonPath("$.months[3].incomingTransactionsSize").value(2))
                .andExpect(jsonPath("$.months[3].outgoingTransactionsSize").value(1))
                .andExpect(jsonPath("$.months[3].totalIncoming").value("1000.01"))
                .andExpect(jsonPath("$.months[3].totalOutgoing").value("5000.05"));
    }
    @NonNull
    private ClientEntity createClientEntityClient3() {
        ClientEntity client = new ClientEntity();
        client.setId(SOME_CLIENT_ID_3);
        client.setAdditionalTextReport(SOME_CLIENT_ADDITIONAL_TEXT);
        client.setSiteTags("NL");
        client.setName(SOME_CLIENT_3_NAME);
        client.setDefaultLanguage(ClientLanguage.NL);
        client.setPDScoreFeatureToggle(true);
        client.setApiTokenFeatureToggle(true);
        client.setCategoryFeatureToggle(true);
        client.setMonthsFeatureToggle(true);
        client.setOverviewFeatureToggle(true);
        return client;
    }
}
