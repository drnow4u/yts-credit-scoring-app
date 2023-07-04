package com.yolt.creditscoring.controller.admin.users;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.creditscoring.IntegrationTest;
import com.yolt.creditscoring.controller.admin.clienttoken.ClientTokenController;
import com.yolt.creditscoring.service.clienttoken.model.ClientTokenPermission;
import com.yolt.creditscoring.service.clienttoken.model.ClientTokenRepository;
import com.yolt.creditscoring.service.creditscore.model.*;
import com.yolt.creditscoring.service.creditscore.recurringtransactions.RecurringTransactionsMonthlyReportEntity;
import com.yolt.creditscoring.service.creditscore.recurringtransactions.RecurringTransactionsMonthlyReportRepository;
import com.yolt.creditscoring.service.estimate.provider.dto.RiskClassification;
import com.yolt.creditscoring.service.estimate.storage.EstimateEntity;
import com.yolt.creditscoring.service.estimate.storage.EstimateRepository;
import com.yolt.creditscoring.service.securitymodule.jwt.JwtCreationService;
import com.yolt.creditscoring.service.securitymodule.vault.VaultSecretKeyService;
import com.yolt.creditscoring.service.user.model.CreditScoreUser;
import com.yolt.creditscoring.service.user.model.CreditScoreUserRepository;
import com.yolt.creditscoring.service.user.model.InvitationStatus;
import com.yolt.creditscoring.service.userjourney.JourneyStatus;
import com.yolt.creditscoring.service.userjourney.model.UserJourneyRepository;
import org.assertj.core.groups.Tuple;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.jwk.RsaJwkGenerator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static com.yolt.creditscoring.TestUtils.*;
import static com.yolt.creditscoring.configuration.security.admin.TestUtils.OAUTH_ADMIN_USER_CLIENT_ADMIN;
import static com.yolt.creditscoring.controller.SecurityHelper.hasSecurityHeaderSetup;
import static com.yolt.creditscoring.controller.admin.clienttoken.ClientTokenController.CREATE_TOKEN_ENDPOINT;
import static com.yolt.creditscoring.controller.admin.users.UserManagementController.*;
import static com.yolt.creditscoring.service.clienttoken.model.ClientTokenPermission.DOWNLOAD_REPORT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.assertj.core.api.BDDAssertions.then;
import static org.hamcrest.Matchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@IntegrationTest
class UserManagementControllerIT {

    @Autowired
    private CreditScoreUserRepository creditScoreUserRepository;

    @Autowired
    private CreditScoreReportRepository creditScoreReportRepository;

    @Autowired
    EstimateRepository estimateRepository;

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

    @MockBean
    private SesClient sesClient;

    @SpyBean
    VaultSecretKeyService vaultSecretKeyService;

    @AfterEach
    void afterTest() {
        creditScoreUserRepository.deleteAll();
        userJourneyRepository.deleteAll();
        creditScoreReportRepository.deleteAll();
        cycleTransactionsMonthlyReportRepository.deleteAll();
        estimateRepository.deleteAll();
        clientTokenRepository.deleteAll();
    }

    @Test
    void shouldViewUsersPagesForLoggedClient() throws Exception {
        // Given
        prepareCreditScoreUsers();
        prepareAdditionalUsersToVerifyPagination();

        // When
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>(2);
        params.add("page", "1");
        params.add("sort", "dateTimeInvited,DESC");

        ResultActions response = mvc.perform(get(GET_USERS_ENDPOINT).queryParams(params)
                .header(HttpHeaders.AUTHORIZATION, jwtCreationService.createAdminToken(OAUTH_ADMIN_USER_CLIENT_ADMIN)));

        // Then
        hasSecurityHeaderSetup(response)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].email", equalTo(SOME_USER_EMAIL_2)))
                .andExpect(jsonPath("$.[0].name", equalTo(SOME_USER_NAME_2)))
                .andExpect(jsonPath("$.[0].dateInvited", equalTo("2020-11-01T11:00:00Z")))
                .andExpect(jsonPath("$.[0].dateStatusUpdated", equalTo("2020-11-01T11:00:00Z")))
                .andExpect(jsonPath("$.[0].status", equalTo("EXPIRED")))
                .andExpect(jsonPath("$.[0].adminEmail", equalTo("adminuser@test.com")))
                .andExpect(jsonPath("$.[1].email", equalTo(SOME_USER_EMAIL)))
                .andExpect(jsonPath("$.[1].name", equalTo(SOME_USER_NAME)))
                .andExpect(jsonPath("$.[1].dateInvited", equalTo("2020-11-01T10:00:00Z")))
                .andExpect(jsonPath("$.[1].dateStatusUpdated", equalTo("2020-11-01T10:00:00Z")))
                .andExpect(jsonPath("$.[1].status", equalTo("INVITED")))
                .andExpect(jsonPath("$.[1].adminEmail", equalTo("admin2@example.com")));
    }

    /**
     * Happy flow
     */
    @ParameterizedTest
    @ValueSource(strings = {
            SOME_USER_NAME,
            "John van Doe",
            "Józef Brzęczyszczykiewicz",
            "D'Hondt",
            "Stoové",
            "Jong-a-Pin",
            "Van 't Schip"
    })
    void shouldInviteUsers(String userName) throws Exception {
        // When
        String request = """
                    {
                        "name": "%s",
                        "email": "%s",
                        "clientEmailId": "%s"
                    }
                """.formatted(userName, SOME_USER_EMAIL, SOME_CLIENT_EMAIL_ID);

        ResultActions perform = mvc.perform(post(INVITE_USER_ENDPOINT)
                .header(HttpHeaders.AUTHORIZATION, jwtCreationService.createAdminToken(OAUTH_ADMIN_USER_CLIENT_ADMIN))
                .contentType(MediaType.APPLICATION_JSON)
                .content(request));

        // Then
        hasSecurityHeaderSetup(perform)
                .andExpect(status().isOk());

        Iterable<CreditScoreUser> result = creditScoreUserRepository.findAll();
        assertThat(result).hasSize(1);
        ArgumentCaptor<SendEmailRequest> sendEmailRequestCaptor = ArgumentCaptor.forClass(SendEmailRequest.class);
        assertThat(result).extracting("name", "email", "adminEmail")
                .contains(tuple(userName, SOME_USER_EMAIL, "adminuser@test.com"));

        verify(sesClient).sendEmail(sendEmailRequestCaptor.capture());
        SendEmailRequest emailRequest = sendEmailRequestCaptor.getValue();
        assertThat(emailRequest.source()).isEqualTo("Cashflow Analyser <no-reply-cashflow-analyser@yolt.com>");
        assertThat(emailRequest.destination().toAddresses()).containsOnly(SOME_USER_EMAIL);
        assertThat(emailRequest.message().subject().data()).isEqualTo("Uitnodiging voor de Cashflow Analyser");
        assertThat(emailRequest.message().body().html().data()).contains(
                "Beste <span>" + thymeleafHtmlCharacterEncode(userName) + "</span>,",
                "<a target=\"_blank\" href=\"http://localhost/consent/");

        then(userJourneyRepository.findAll())
                .extracting("clientId", "status")
                .containsExactly(Tuple.tuple(SOME_CLIENT_ID, JourneyStatus.INVITED));
    }

    /**
     * Additional to happy flow {@link #shouldInviteUsers(String)}, a client email id is optional if the client only has 1 email.
     */
    @Test
    void given_AClientWithOneEmail_when_AUserIsInvitedWithoutClientEmailId_then_theUserShouldBeInvited() throws Exception {

        // Given a client admin (SOME_CLIENT_ADMIN_IDP_ID), of a client (SOME_CLIENT_ID) with only 1 email SOME_CLIENT_EMAIL_ID. See afterMigrate.sql.

        // When
        String requestWithoutClientEmailId = """
                    {
                        "name": "%s",
                        "email": "%s"
                    }
                """.formatted(SOME_USER_NAME, SOME_USER_EMAIL);
        ResultActions perform = mvc.perform(post(INVITE_USER_ENDPOINT)
                .header(HttpHeaders.AUTHORIZATION, jwtCreationService.createAdminToken(OAUTH_ADMIN_USER_CLIENT_ADMIN))
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestWithoutClientEmailId));

        // Then
        hasSecurityHeaderSetup(perform)
                .andExpect(status().isOk());

        // The user is created..
        Iterable<CreditScoreUser> result = creditScoreUserRepository.findAll();
        assertThat(result).hasSize(1);
        assertThat(result).extracting("name", "email", "adminEmail", "clientEmailId")
                .contains(tuple(SOME_USER_NAME, SOME_USER_EMAIL, "adminuser@test.com", SOME_CLIENT_EMAIL_ID));

        // And the email has been sent
        ArgumentCaptor<SendEmailRequest> sendEmailRequestCaptor = ArgumentCaptor.forClass(SendEmailRequest.class);
        verify(sesClient).sendEmail(sendEmailRequestCaptor.capture());
        SendEmailRequest emailRequest = sendEmailRequestCaptor.getValue();
        assertThat(emailRequest.source()).isEqualTo("Cashflow Analyser <no-reply-cashflow-analyser@yolt.com>");
        assertThat(emailRequest.destination().toAddresses()).containsOnly(SOME_USER_EMAIL);
    }

    private static String thymeleafHtmlCharacterEncode(String html) {
        return html.replace("'", "&#39;");
    }

    @Test
    void shouldResendUserInvitation() throws Exception {
        // Given
        prepareCreditScoreUsers();
        ArgumentCaptor<SendEmailRequest> sendEmailRequestCaptor = ArgumentCaptor.forClass(SendEmailRequest.class);

        // When
        ResultActions perform = mvc.perform(put(RE_INVITE_USER_BY_USERID_ENDPOINT, SOME_USER_ID_2)
                .header(HttpHeaders.AUTHORIZATION, jwtCreationService.createAdminToken(OAUTH_ADMIN_USER_CLIENT_ADMIN)));

        hasSecurityHeaderSetup(perform)
                .andExpect(status().isOk());

        // Then
        CreditScoreUser result = creditScoreUserRepository.findById(SOME_USER_ID_2).orElseThrow();
        assertThat(result.getName()).isEqualTo(SOME_USER_NAME_2);
        assertThat(result.getEmail()).isEqualTo(SOME_USER_EMAIL_2);
        assertThat(result.getStatus()).isEqualTo(InvitationStatus.INVITED);

        assertThat(result.getInvitationHash()).isNotEqualTo(SOME_USER_HASH_2);
        assertThat(result.getDateTimeStatusChange()).isNotEqualTo(SOME_FIXED_TEST_DATE);
        assertThat(result.getAdminEmail()).isEqualTo(SOME_CLIENT_ADMIN_EMAIL);

        verify(sesClient).sendEmail(sendEmailRequestCaptor.capture());
        SendEmailRequest emailRequest = sendEmailRequestCaptor.getValue();
        assertThat(emailRequest.source()).isEqualTo("Cashflow Analyser <no-reply-cashflow-analyser@yolt.com>");
        assertThat(emailRequest.destination().toAddresses()).containsOnly(SOME_USER_EMAIL_2);
        assertThat(emailRequest.message().subject().data()).isEqualTo("Uitnodiging voor de Cashflow Analyser");
        assertThat(emailRequest.message().body().html().data()).contains(
                "Beste <span>User D&#39;Second</span>,",
                "<a target=\"_blank\" href=\"http://localhost/consent/");

        then(userJourneyRepository.findAll()).isEmpty();
    }

    @Test
    void shouldReturnBadRequestWhenResendingInvitationForUserWithNotExpiredStatus() throws Exception {
        // Given
        prepareCreditScoreUsers();

        // When
        ResultActions perform = mvc.perform(put(RE_INVITE_USER_BY_USERID_ENDPOINT, SOME_USER_ID)
                .header(HttpHeaders.AUTHORIZATION, jwtCreationService.createAdminToken(OAUTH_ADMIN_USER_CLIENT_ADMIN)));

        // Then
        hasSecurityHeaderSetup(perform)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode", notNullValue()));
    }

    @Test
    void shouldReturnBadRequestWhenResendingInvitationForUserBelongToOtherClient() throws Exception {
        // Given
        prepareCreditScoreUsers();

        // When
        ResultActions perform = mvc.perform(put(RE_INVITE_USER_BY_USERID_ENDPOINT, SOME_USER_ID_3)
                .header(HttpHeaders.AUTHORIZATION, jwtCreationService.createAdminToken(OAUTH_ADMIN_USER_CLIENT_ADMIN)));

        // Then
        hasSecurityHeaderSetup(perform)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode", notNullValue()));
    }

    @Test
    void shouldDeleteUser() throws Exception {
        // Given
        prepareCreditScoreUsers();

        // When
        ResultActions perform = mvc.perform(delete(DELETE_USER_BY_USERID_ENDPOINT, SOME_USER_ID)
                .header(HttpHeaders.AUTHORIZATION, jwtCreationService.createAdminToken(OAUTH_ADMIN_USER_CLIENT_ADMIN)));

        // Then
        hasSecurityHeaderSetup(perform)
                .andExpect(status().isOk());

        Iterable<CreditScoreUser> users = creditScoreUserRepository.findAll();
        assertThat(users).hasSize(2);

        Iterable<CreditScoreReport> reports = creditScoreReportRepository.findAll();
        assertThat(reports).hasSize(1);

        Optional<EstimateEntity> estimateReport = estimateRepository.findByUserId(SOME_USER_ID);
        assertThat(estimateReport).isNotPresent();
    }

    @Test
    void shouldNotDeleteUserForOtherClients() throws Exception {
        // Given
        prepareCreditScoreUsers();

        // When
        final ResultActions perform = mvc.perform(delete(DELETE_USER_BY_USERID_ENDPOINT, SOME_USER_ID_3)
                .header(HttpHeaders.AUTHORIZATION, jwtCreationService.createAdminToken(OAUTH_ADMIN_USER_CLIENT_ADMIN)));

        // Then
        hasSecurityHeaderSetup(perform)
                .andExpect(status().isNotFound());

        Optional<CreditScoreUser> user = creditScoreUserRepository.findById(SOME_USER_ID_3);
        assertThat(user).isPresent();
    }

    @Test
    void shouldDeleteUserWithoutYoltAccountCreated() throws Exception {
        // Given
        prepareCreditScoreUsers();

        // When
        ResultActions perform = mvc.perform(delete(DELETE_USER_BY_USERID_ENDPOINT, SOME_USER_ID_2)
                .header(HttpHeaders.AUTHORIZATION, jwtCreationService.createAdminToken(OAUTH_ADMIN_USER_CLIENT_ADMIN)));

        // Then
        hasSecurityHeaderSetup(perform)
                .andExpect(status().isOk());

        Iterable<CreditScoreUser> users = creditScoreUserRepository.findAll();
        assertThat(users).hasSize(2);

        assertThat(creditScoreReportRepository.findByCreditScoreUserId(SOME_USER_ID_2)).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"user@example.com.", "user@example..com", "user@.example.com", "user@@example.com", "<h1>user</h1>@example.com"})
    void shouldReturnViolationMessagesWhenInviteUserWithNotValidEmail(String email) throws Exception {

        // When
        var result = mvc.perform(post(INVITE_USER_ENDPOINT)
                .header(HttpHeaders.AUTHORIZATION, jwtCreationService.createAdminToken(OAUTH_ADMIN_USER_CLIENT_ADMIN))
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT_LANGUAGE, "pl")
                .content(String.format("{ \"name\": \"%s\", \"email\": \"%s\", \"clientEmailId\": \"%s\" }", SOME_USER_NAME, email, SOME_CLIENT_EMAIL_ID)));

        // Then
        hasSecurityHeaderSetup(result)
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.violations.[0].fieldName", equalTo("email")))
                .andExpect(jsonPath("$.violations.[0].message", equalTo("must be a well-formed email address")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"X", "<h1>John Doe</h1>", " ", USER_NAME_LONGER_256})
    void shouldReturnViolationMessagesWhenInviteUserWithNotValidName(String name) throws Exception {
        // When
        var result = mvc.perform(post(INVITE_USER_ENDPOINT)
                .header(HttpHeaders.AUTHORIZATION, jwtCreationService.createAdminToken(OAUTH_ADMIN_USER_CLIENT_ADMIN))
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT_LANGUAGE, "pl")
                .content(String.format("{ \"name\": \"%s\", \"email\": \"%s\", \"clientEmailId\": \"%s\" }", name, SOME_USER_EMAIL, SOME_CLIENT_EMAIL_ID)));

        // Then
        hasSecurityHeaderSetup(result)
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.violations.[0].fieldName", equalTo("name")))
                .andExpect(jsonPath("$.violations.[0].message", equalTo("must be a well-formed user name")));
    }

    @Test
    void shouldFetchTheUserCreditScoreReport() throws Exception {
        // Given
        prepareCreditScoreUsers();
        prepareCreditScoreMonths();

        // When
        ResultActions perform = mvc.perform(get(GET_USER_REPORT_BY_USERID_ENDPOINT, SOME_USER_ID)
                .header(HttpHeaders.AUTHORIZATION, jwtCreationService.createAdminToken(OAUTH_ADMIN_USER_CLIENT_ADMIN)));

        // Then
        hasSecurityHeaderSetup(perform)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userEmail", equalTo("user@email.com")))
                .andExpect(jsonPath("$.adminReport.oldestTransactionDate", equalTo("2020-12-01")))
                .andExpect(jsonPath("$.adminReport.newestTransactionDate", equalTo("2020-12-31")))
                .andExpect(jsonPath("$.adminReport.currency", equalTo("EUR")))
                .andExpect(jsonPath("$.adminReport.iban", equalTo("NL79ABNA12345678901")))
                .andExpect(jsonPath("$.adminReport.creditLimit", equalTo("-1000.00")))
                .andExpect(jsonPath("$.adminReport.initialBalance", equalTo("5000.00")))
                .andExpect(jsonPath("$.adminReport.transactionsSize", equalTo(100)))
                .andExpect(jsonPath("$.adminReport.accountHolder", equalTo("Account Holder")))

                .andExpect(jsonPath("$.publicKey", equalTo("oWXFYXTVDnjQUQekNSegOFRYnMdBZNvgwdz0Em6j6Ih70xMvajIjdHYvuXlaeLmiKQ0aGYGmAws-ueHItAWug6IkJtayg7ZbCnFu6QGpNxunrESYpiZ8hpg_UbPRG3g4jFwUL_8igCbppvmEi86x-q-EMETbSP556xTF090jPplwZU96921dElYcY-_LE_6-tvkCuE0lXnpjLqM_LDNONP0sojLxD0_6DrEzPUlBnIW6IQ0RWgqwQ9Je8HV3_CWSdevHWWRCIbCNBMrJ2ZpgRVQJwXY2P_sUZT1NNW3xK4EOTLW9RtWbDArmoBJ60KKJkWj_XmCb2Qhsyk3j4daVDw")))
                .andExpect(jsonPath("$.signature", equalTo("Kxf6gtjpDwubibfnqaTEYavr/yE9XAEY7FmtUCZLmQyIzlqKJS7GSV6nzOCbsZxsNzpOhTQIrmeDIq+9HwBb90qqy/6jHX3yy8WtO93vScjyC6/H95Taf2h1+lHWyEhEU+jRB4glg5PuTREToPcYRmVUDIyLtI7s6WF7SR1nHjL8t0eDRwVTRQE8O5ndzdwIKAgIUpdzitNsnSIfkGHKZwlABcB/bRdJ/tsnpIq+zDD6g0qFjD3x1esAPWvtzwGOEOf4hJc53GyHet1UE2gXQmdGzGti0mt5mRXQq8eVJn/hip2tHfYnDq6v1x0LnOcH3izSTIl7Nkvp9YCmpXjejytInEI/wN/G5s6UDstUUs0U0l3w7Ixa0jQVoVfYWBPt5e8Jn0ru3OPiYyQYA/+cVqU3OrpvCnpHSJaGlysaCn8SOTH6YFKV49xz+J2flHm2Xx+TwHTSCdmkR2SWfLyRjRmvPduhatv1p++4R9lD29Nc4mm0r4/dWmh7WNd5bGh2FHJiup3GcEcLx77Uc1mk5mOjN7CHbU4d78rFgf6YLCcjqh+N0FQAv67BPIlrTfbN6kP41BhBU4iSKkVlYwzBOOJAw45kfxPt3KBXnBRWAdXol91SF2RATw5YU7tmoypcMOxXZQBNRbwXLEJjUZd7bevDodpvaW8AjKKH1uMY7aI=")))
                .andExpect(jsonPath("$.signatureJsonPaths[0]", equalTo("$['userId']")))
                .andExpect(jsonPath("$.signatureJsonPaths[1]", equalTo("$['iban']")))
                .andExpect(jsonPath("$.signatureJsonPaths[2]", equalTo("$['initialBalance']")))
                .andExpect(jsonPath("$.signatureJsonPaths[3]", equalTo("$['currency']")))
                .andExpect(jsonPath("$.signatureJsonPaths[4]", equalTo("$['newestTransactionDate']")))
                .andExpect(jsonPath("$.signatureJsonPaths[5]", equalTo("$['oldestTransactionDate']")))
                .andExpect(jsonPath("$.signatureJsonPaths[6]", equalTo("$['transactionsSize']")));
    }

    @Test
    void shouldFetchTheUserCreditScoreReportWhenSigningKeyRotate() throws Exception {
        // Given
        prepareCreditScoreUsers();
        prepareCreditScoreMonths();

        RsaJsonWebKey rsaJsonWebKeyNew = RsaJwkGenerator.generateJwk(2048);
        UUID signingKeyIdNew = UUID.fromString("6196257e-58f6-4e29-8d7f-31e00867b214");
        rsaJsonWebKeyNew.setKeyId(signingKeyIdNew.toString());
        given(vaultSecretKeyService.getReportSignKeyId()).willReturn(signingKeyIdNew);
        given(vaultSecretKeyService.getReportSignPublicKey()).willReturn(rsaJsonWebKeyNew.getRsaPublicKey());

        // When
        ResultActions perform = mvc.perform(get(GET_USER_REPORT_BY_USERID_ENDPOINT, SOME_USER_ID)
                .header(HttpHeaders.AUTHORIZATION, jwtCreationService.createAdminToken(OAUTH_ADMIN_USER_CLIENT_ADMIN)));

        // Then
        hasSecurityHeaderSetup(perform)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userEmail", equalTo("user@email.com")))
                .andExpect(jsonPath("$.adminReport.oldestTransactionDate", equalTo("2020-12-01")))
                .andExpect(jsonPath("$.adminReport.newestTransactionDate", equalTo("2020-12-31")))
                .andExpect(jsonPath("$.adminReport.currency", equalTo("EUR")))
                .andExpect(jsonPath("$.adminReport.iban", equalTo("NL79ABNA12345678901")))
                .andExpect(jsonPath("$.adminReport.creditLimit", equalTo("-1000.00")))
                .andExpect(jsonPath("$.adminReport.initialBalance", equalTo("5000.00")))
                .andExpect(jsonPath("$.adminReport.transactionsSize", equalTo(100)))
                .andExpect(jsonPath("$.adminReport.accountHolder", equalTo("Account Holder")))

                .andExpect(jsonPath("$.publicKey", equalTo("oWXFYXTVDnjQUQekNSegOFRYnMdBZNvgwdz0Em6j6Ih70xMvajIjdHYvuXlaeLmiKQ0aGYGmAws-ueHItAWug6IkJtayg7ZbCnFu6QGpNxunrESYpiZ8hpg_UbPRG3g4jFwUL_8igCbppvmEi86x-q-EMETbSP556xTF090jPplwZU96921dElYcY-_LE_6-tvkCuE0lXnpjLqM_LDNONP0sojLxD0_6DrEzPUlBnIW6IQ0RWgqwQ9Je8HV3_CWSdevHWWRCIbCNBMrJ2ZpgRVQJwXY2P_sUZT1NNW3xK4EOTLW9RtWbDArmoBJ60KKJkWj_XmCb2Qhsyk3j4daVDw")))
                .andExpect(jsonPath("$.signature", equalTo("Kxf6gtjpDwubibfnqaTEYavr/yE9XAEY7FmtUCZLmQyIzlqKJS7GSV6nzOCbsZxsNzpOhTQIrmeDIq+9HwBb90qqy/6jHX3yy8WtO93vScjyC6/H95Taf2h1+lHWyEhEU+jRB4glg5PuTREToPcYRmVUDIyLtI7s6WF7SR1nHjL8t0eDRwVTRQE8O5ndzdwIKAgIUpdzitNsnSIfkGHKZwlABcB/bRdJ/tsnpIq+zDD6g0qFjD3x1esAPWvtzwGOEOf4hJc53GyHet1UE2gXQmdGzGti0mt5mRXQq8eVJn/hip2tHfYnDq6v1x0LnOcH3izSTIl7Nkvp9YCmpXjejytInEI/wN/G5s6UDstUUs0U0l3w7Ixa0jQVoVfYWBPt5e8Jn0ru3OPiYyQYA/+cVqU3OrpvCnpHSJaGlysaCn8SOTH6YFKV49xz+J2flHm2Xx+TwHTSCdmkR2SWfLyRjRmvPduhatv1p++4R9lD29Nc4mm0r4/dWmh7WNd5bGh2FHJiup3GcEcLx77Uc1mk5mOjN7CHbU4d78rFgf6YLCcjqh+N0FQAv67BPIlrTfbN6kP41BhBU4iSKkVlYwzBOOJAw45kfxPt3KBXnBRWAdXol91SF2RATw5YU7tmoypcMOxXZQBNRbwXLEJjUZd7bevDodpvaW8AjKKH1uMY7aI=")))
                .andExpect(jsonPath("$.signatureJsonPaths[0]", equalTo("$['userId']")))
                .andExpect(jsonPath("$.signatureJsonPaths[1]", equalTo("$['iban']")))
                .andExpect(jsonPath("$.signatureJsonPaths[2]", equalTo("$['initialBalance']")))
                .andExpect(jsonPath("$.signatureJsonPaths[3]", equalTo("$['currency']")))
                .andExpect(jsonPath("$.signatureJsonPaths[4]", equalTo("$['newestTransactionDate']")))
                .andExpect(jsonPath("$.signatureJsonPaths[5]", equalTo("$['oldestTransactionDate']")))
                .andExpect(jsonPath("$.signatureJsonPaths[6]", equalTo("$['transactionsSize']")));
    }

    @Test
    void shouldFetchTheUserCreditScoreOverviewReport() throws Exception {
        // Given
        prepareCreditScoreUsers();
        prepareCreditScoreMonths();

        // When
        ResultActions perform = mvc.perform(get(GET_USER_OVERVIEW_BY_USERID_ENDPOINT, SOME_USER_ID)
                .header(HttpHeaders.AUTHORIZATION, jwtCreationService.createAdminToken(OAUTH_ADMIN_USER_CLIENT_ADMIN)));

        // Then
        hasSecurityHeaderSetup(perform)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.averageRecurringIncome", equalTo("12.43")))
                .andExpect(jsonPath("$.averageRecurringCosts", equalTo("5.83")))

                .andExpect(jsonPath("$.incomingTransactionsSize", equalTo(12)))
                .andExpect(jsonPath("$.outgoingTransactionsSize", equalTo(8)))
                .andExpect(jsonPath("$.monthlyAverageIncome", equalTo("2250.00")))
                .andExpect(jsonPath("$.monthlyAverageCost", equalTo("916.67")))
                .andExpect(jsonPath("$.totalIncomeAmount", equalTo("27000.00")))
                .andExpect(jsonPath("$.totalOutgoingAmount", equalTo("11000.00")))
                .andExpect(jsonPath("$.averageIncomeTransactionAmount", equalTo("2250.00")))
                .andExpect(jsonPath("$.averageOutcomeTransactionAmount", equalTo("1375.00")))
                .andExpect(jsonPath("$.vatTotalPayments", equalTo(3)))
                .andExpect(jsonPath("$.vatAverage", equalTo("333.33")))
                .andExpect(jsonPath("$.totalCorporateTax", equalTo("0")))
                .andExpect(jsonPath("$.totalTaxReturns", equalTo("6000.00")));
    }

    @Test
    void shouldFetchTheUserCreditScoreMonthsReport() throws Exception {
        // Given
        prepareCreditScoreUsers();
        prepareCreditScoreMonths();

        // When
        ResultActions perform = mvc.perform(get(GET_USER_MONTHS_BY_USERID_ENDPOINT, SOME_USER_ID)
                .header(HttpHeaders.AUTHORIZATION, jwtCreationService.createAdminToken(OAUTH_ADMIN_USER_CLIENT_ADMIN)));

        // Then
        hasSecurityHeaderSetup(perform)
                .andExpect(status().isOk())

                .andExpect(jsonPath("$.monthlyReports[0].year", equalTo(2019)))
                .andExpect(jsonPath("$.monthlyReports[0].month", equalTo(12)))
                .andExpect(jsonPath("$.monthlyReports[0].highestBalance", equalTo("500.00")))
                .andExpect(jsonPath("$.monthlyReports[0].lowestBalance", equalTo("300.00")))
                .andExpect(jsonPath("$.monthlyReports[0].averageBalance", equalTo("600.00")))
                .andExpect(jsonPath("$.monthlyReports[0].totalIncoming", equalTo("17000.00")))
                .andExpect(jsonPath("$.monthlyReports[0].totalOutgoing", equalTo("6000.00")))
                .andExpect(jsonPath("$.monthlyReports[0].incomingTransactionsSize", equalTo(7)))
                .andExpect(jsonPath("$.monthlyReports[0].outgoingTransactionsSize", equalTo(5)))

                .andExpect(jsonPath("$.monthlyReports[1].year", equalTo(2020)))
                .andExpect(jsonPath("$.monthlyReports[1].month", equalTo(11)))
                .andExpect(jsonPath("$.monthlyReports[1].highestBalance", equalTo("10000.00")))
                .andExpect(jsonPath("$.monthlyReports[1].lowestBalance", equalTo("5000.00")))
                .andExpect(jsonPath("$.monthlyReports[1].averageBalance", equalTo("6000.00")))
                .andExpect(jsonPath("$.monthlyReports[1].totalIncoming", equalTo("17000.00")))
                .andExpect(jsonPath("$.monthlyReports[1].totalOutgoing", equalTo("6000.00")))
                .andExpect(jsonPath("$.monthlyReports[1].incomingTransactionsSize", equalTo(6)))
                .andExpect(jsonPath("$.monthlyReports[1].outgoingTransactionsSize", equalTo(4)))

                .andExpect(jsonPath("$.monthlyReports[2].year", equalTo(2020)))
                .andExpect(jsonPath("$.monthlyReports[2].month", equalTo(12)))
                .andExpect(jsonPath("$.monthlyReports[2].highestBalance", equalTo("15000.00")))
                .andExpect(jsonPath("$.monthlyReports[2].lowestBalance", equalTo("10000.00")))
                .andExpect(jsonPath("$.monthlyReports[2].averageBalance", equalTo("12000.00")))
                .andExpect(jsonPath("$.monthlyReports[2].totalIncoming", equalTo("10000.00")))
                .andExpect(jsonPath("$.monthlyReports[2].totalOutgoing", equalTo("5000.00")))
                .andExpect(jsonPath("$.monthlyReports[2].incomingTransactionsSize", equalTo(6)))
                .andExpect(jsonPath("$.monthlyReports[2].outgoingTransactionsSize", equalTo(4)))

                .andExpect(jsonPath("$.monthlyReports[3].year", equalTo(2021)))
                .andExpect(jsonPath("$.monthlyReports[3].month", equalTo(1)))
                .andExpect(jsonPath("$.monthlyReports[3].highestBalance", equalTo("2000.02")))
                .andExpect(jsonPath("$.monthlyReports[3].lowestBalance", equalTo("30000.03")))
                .andExpect(jsonPath("$.monthlyReports[3].averageBalance", equalTo("4000.04")))
                .andExpect(jsonPath("$.monthlyReports[3].totalIncoming", equalTo("1000.01")))
                .andExpect(jsonPath("$.monthlyReports[3].totalOutgoing", equalTo("5000.05")))
                .andExpect(jsonPath("$.monthlyReports[3].incomingTransactionsSize", equalTo(2)))
                .andExpect(jsonPath("$.monthlyReports[3].outgoingTransactionsSize", equalTo(1)));
    }

    @Test
    void shouldNotBeAbleFetchUserCreditReportIfUserIsNotAssignToTheSameClientAsAdminUser() throws Exception {
        // Given
        prepareCreditScoreUsers();

        // When
        final ResultActions perform = mvc.perform(get(GET_USER_REPORT_BY_USERID_ENDPOINT, SOME_USER_ID_3)
                .header(HttpHeaders.AUTHORIZATION, jwtCreationService.createAdminToken(OAUTH_ADMIN_USER_CLIENT_ADMIN)));

        // Then
        hasSecurityHeaderSetup(perform)
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldFetchUserCategories() throws Exception {
        // Given
        prepareCreditScoreUsers();

        // When
        ResultActions perform = mvc.perform(get(GET_USER_CATEGORIES_BY_USERID_ENDPOINT, SOME_USER_ID)
                .header(HttpHeaders.AUTHORIZATION, jwtCreationService.createAdminToken(OAUTH_ADMIN_USER_CLIENT_ADMIN)));

        // Then
        hasSecurityHeaderSetup(perform)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.categoryName == 'OTHER_EXPENSES')].totalTransactions", contains(equalTo(6))))
                .andExpect(jsonPath("$[?(@.categoryName == 'OTHER_EXPENSES')].averageTransactionAmount", contains(equalTo("1666.66"))))
                .andExpect(jsonPath("$[?(@.categoryName == 'OTHER_EXPENSES')].totalTransactionAmount", contains(equalTo(("10000.00")))))
                .andExpect(jsonPath("$[?(@.categoryName == 'OTHER_EXPENSES')].categoryType", contains(equalTo(("OUTGOING")))))
                .andExpect(jsonPath("$[?(@.categoryName == 'OTHER_INCOME')].totalTransactions", contains(equalTo((3)))))
                .andExpect(jsonPath("$[?(@.categoryName == 'OTHER_INCOME')].averageTransactionAmount", contains(equalTo(("3666.66")))))
                .andExpect(jsonPath("$[?(@.categoryName == 'OTHER_INCOME')].totalTransactionAmount", contains(equalTo(("11000.00")))))
                .andExpect(jsonPath("$[?(@.categoryName == 'REVENUE')].totalTransactions", contains(equalTo((1)))))
                .andExpect(jsonPath("$[?(@.categoryName == 'REVENUE')].averageTransactionAmount", contains(equalTo(("10000.00")))))
                .andExpect(jsonPath("$[?(@.categoryName == 'REVENUE')].totalTransactionAmount", contains(equalTo(("10000.00")))))
                .andExpect(jsonPath("$[?(@.categoryName == 'REVENUE')].categoryType", contains(equalTo(("INCOMING")))))
                .andExpect(jsonPath("$[?(@.categoryName == 'SALES_TAX')].totalTransactions", contains(equalTo((3)))))
                .andExpect(jsonPath("$[?(@.categoryName == 'SALES_TAX')].averageTransactionAmount", contains(equalTo(("333.33")))))
                .andExpect(jsonPath("$[?(@.categoryName == 'SALES_TAX')].totalTransactionAmount", contains(equalTo(("1000.00")))))
                .andExpect(jsonPath("$[?(@.categoryName == 'SALES_TAX')].categoryType", contains(equalTo(("OUTGOING")))))
                .andExpect(jsonPath("$[?(@.categoryName == 'TAX_RETURNS')].totalTransactions", contains(equalTo((6)))))
                .andExpect(jsonPath("$[?(@.categoryName == 'TAX_RETURNS')].averageTransactionAmount", contains(equalTo(("1000.00")))))
                .andExpect(jsonPath("$[?(@.categoryName == 'TAX_RETURNS')].totalTransactionAmount", contains(equalTo(("6000.00")))))
                .andExpect(jsonPath("$[?(@.categoryName == 'TAX_RETURNS')].categoryType", contains(equalTo(("INCOMING")))));
    }

    @Test
    void shouldDownloadUserReport() throws Exception {
        // Given
        prepareCreditScoreUsers();
        prepareCreditScoreMonths();

        // When
        ResultActions perform = mvc.perform(get(DOWNLOAD_REPORT_BY_USERID_ENDPOINT, SOME_USER_ID)
                .header(HttpHeaders.AUTHORIZATION, jwtCreationService.createAdminToken(OAUTH_ADMIN_USER_CLIENT_ADMIN)));

        // Then
        List<String> fileNames = new LinkedList<>();

        hasSecurityHeaderSetup(perform)
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/zip"))
                .andDo(result -> {
                    byte[] content = result.getResponse().getContentAsByteArray();
                    ZipInputStream zipInputStream = new ZipInputStream(new ByteArrayInputStream(content));
                    for (ZipEntry entry; (entry = zipInputStream.getNextEntry()) != null; ) {
                        fileNames.add(entry.getName());
                    }
                });

        assertThat(fileNames)
                .hasSize(4)
                .contains("Overview.json", "CategoriesReport.csv", "NL79ABNA12345678901_2020-12-31_2020-12-01_Kxf6gtj_1uMY7aI=_monthly.csv", "EstimateReport.json");
    }

    @Test
    void shouldNotDownloadReportForNotExistingUser() throws Exception {
        // Given
        String fakeId = "347f7d89-575d-4a01-8562-f43868072f77";

        // When
        ResultActions perform = mvc.perform(get(DOWNLOAD_REPORT_BY_USERID_ENDPOINT, fakeId)
                .header(HttpHeaders.AUTHORIZATION, jwtCreationService.createAdminToken(OAUTH_ADMIN_USER_CLIENT_ADMIN)));

        hasSecurityHeaderSetup(perform)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorType", equalTo("USER_NOT_FOUND")));
    }

    @Test
    void shouldNotDownloadReportForUserBelongingToOtherClient() throws Exception {
        // Given
        prepareCreditScoreUsers();
        prepareCreditScoreMonths();

        // When
        ResultActions perform = mvc.perform(get(DOWNLOAD_REPORT_BY_USERID_ENDPOINT, SOME_USER_ID_3)
                .header(HttpHeaders.AUTHORIZATION, jwtCreationService.createAdminToken(OAUTH_ADMIN_USER_CLIENT_ADMIN)));

        hasSecurityHeaderSetup(perform)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorType", equalTo("USER_NOT_FOUND")));
    }

    @Test
    void shouldNotDownloadUserReportWithValidServerToServerToken() throws Exception {
        // Given
        prepareCreditScoreUsers();
        prepareCreditScoreMonths();

        // When
        ResultActions perform = mvc.perform(get(DOWNLOAD_REPORT_BY_USERID_ENDPOINT, SOME_USER_ID)
                .header(HttpHeaders.AUTHORIZATION, createServerToServerToken(DOWNLOAD_REPORT)));

        // Then
        hasSecurityHeaderSetup(perform)
                .andExpect(status().isForbidden());
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
                .setClientId(SOME_CLIENT_ID)
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
                .categorizedAmount(Category.TAX_RETURNS, new BigDecimal("6000.00"), 6)
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
                .categorizedAmount(Category.TAX_RETURNS, new BigDecimal("6000.00"), 6)
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
                .setClientId(SOME_CLIENT_ID_2)
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

        estimateRepository.save(new EstimateEntity()
                .setId(UUID.randomUUID())
                .setUserId(SOME_USER_ID)
                .setGrade(RiskClassification.A)
                .setScore(10)
                .setStatus(PdStatus.COMPLETED));
    }

    private void prepareAdditionalUsersToVerifyPagination() {
        List<CreditScoreUser> users = new ArrayList<>();
        for (int userIterator = 1; userIterator <= 20; userIterator++) {
            CreditScoreUser user = new CreditScoreUser()
                    .setId(UUID.randomUUID())
                    .setName(SOME_USER_NAME)
                    .setEmail(SOME_USER_EMAIL + "_" + userIterator)
                    .setDateTimeInvited(SOME_FIXED_TEST_DATE_2.plusHours(userIterator)) //to verify if sorting is working correctly
                    .setDateTimeStatusChange(SOME_FIXED_TEST_DATE)
                    .setStatus(InvitationStatus.INVITED)
                    .setInvitationHash(UUID.randomUUID().toString())
                    .setClientId(SOME_CLIENT_ID)
                    .setYoltUserId(SOME_YOLT_USER_ID)
                    .setAdminEmail(SOME_CLIENT_ADMIN_EMAIL);

            users.add(user);
        }
        creditScoreUserRepository.saveAll(users);
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

    private String createServerToServerToken(ClientTokenPermission permission) throws Exception {

        ResultActions performToken = mvc.perform(post(CREATE_TOKEN_ENDPOINT)
                .header(HttpHeaders.AUTHORIZATION, jwtCreationService.createAdminToken(OAUTH_ADMIN_USER_CLIENT_ADMIN))
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

}
