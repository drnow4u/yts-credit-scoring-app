package com.yolt.creditscoring.flow;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.Option;
import com.yolt.creditscoring.IntegrationTest;
import com.yolt.creditscoring.controller.user.invitation.ConsentViewDTO;
import com.yolt.creditscoring.service.user.model.CreditScoreUser;
import com.yolt.creditscoring.service.user.model.CreditScoreUserRepository;
import com.yolt.creditscoring.service.user.model.InvitationStatus;
import com.yolt.creditscoring.service.userjourney.model.UserJourneyRepository;
import org.apache.http.HttpHeaders;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.jayway.jsonpath.JsonPath.using;
import static com.yolt.creditscoring.TestUtils.*;
import static com.yolt.creditscoring.controller.user.account.CreditScoreUserAccountController.USER_ACCOUNTS_ENDPOINT;
import static com.yolt.creditscoring.controller.user.account.CreditScoreUserAccountController.USER_ACCOUNTS_SELECT_ENDPOINT;
import static com.yolt.creditscoring.controller.user.creditscore.CreditScoreReportController.*;
import static com.yolt.creditscoring.controller.user.invitation.UserInvitationController.USER_CONSENT_ENDPOINT;
import static com.yolt.creditscoring.controller.user.invitation.UserInvitationController.USER_INVITATION_ENDPOINT;
import static com.yolt.creditscoring.controller.user.site.SiteController.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestPropertySource(properties = {
        "yolt.creditScoreExecutor.async=false",})
class UserFlowIT {

    @Autowired
    private CreditScoreUserRepository creditScoreUserRepository;

    @Autowired
    private UserJourneyRepository userJourneyRepository;

    @Autowired
    private MockMvc mvc;

    private ObjectMapper objectMapper;
    private Configuration conf;

    @BeforeEach
    void setUp() {
        WireMock.resetAllRequests();
        objectMapper = new ObjectMapper();
        conf = Configuration.builder()
                .options(Option.AS_PATH_LIST).build();
    }

    @AfterAll
    void afterAll() {
        userJourneyRepository.deleteAll();
        creditScoreUserRepository.deleteAll();
    }

    @Timeout(15)
    @ParameterizedTest
    @MethodSource("testArguments")
    void positiveFlow(String creditReportShareEndpoint, String creditReportShareStatus) throws Exception {
        // Given
        UUID userId = UUID.randomUUID();
        var invitationHash = UUID.randomUUID().toString();
        CreditScoreUser user = new CreditScoreUser()
                .setId(userId)
                .setName(SOME_USER_NAME)
                .setEmail(SOME_USER_EMAIL)
                .setDateTimeInvited(SOME_TEST_DATE)
                .setStatus(InvitationStatus.INVITED)
                .setInvitationHash(invitationHash)
                .setClientId(SOME_CLIENT_ID)
                .setAdminEmail(SOME_CLIENT_ADMIN_EMAIL);

        creditScoreUserRepository.save(user);
        // When click invitation link in e-mail
        final ResultActions perform = mvc.perform(get(USER_INVITATION_ENDPOINT, invitationHash));

        MockHttpServletResponse ret = perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andReturn().getResponse();
        ConsentViewDTO consentViewDTO = objectMapper.readValue(ret.getContentAsString(), ConsentViewDTO.class);
        String token = consentViewDTO.getToken();

        // When consent
        ResultActions performConsent = mvc.perform(post(USER_CONSENT_ENDPOINT)
                        .header("user-agent", SOME_USER_AGENT)
                        .header("x-real-ip", SOME_USER_IP)
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"consent\": \"true\" }"))
                .andExpect(status().isOk());
        assertThat(resultToJsonPath(performConsent)).isNull();

        // When list of bank
        ResultActions performSites = mvc.perform(get(SITES_ENDPOINT)
                        .header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].id", equalTo("497f6eca-6276-4993-bfeb-53cbbbba6f08")))
                .andExpect(jsonPath("$.[0].name", equalTo("Credit Agricole")))
                .andExpect(jsonPath("$.[1].id", equalTo("a2d82da0-9bea-4f55-bd4e-88bd5e919d76")))
                .andExpect(jsonPath("$.[1].name", equalTo("ABM Amro")))
                .andExpect(jsonPath("$.[2].id", equalTo("1e1d7cd1-3652-41ea-8dd3-44c868a7aa75")))
                .andExpect(jsonPath("$.[2].name", equalTo("Barclays")))
                .andExpect(jsonPath("$.[3].id", equalTo("eefed7b0-8e5c-4941-8b30-2f9af7bb6060")))
                .andExpect(jsonPath("$.[3].name", equalTo("HSBC")));

        assertThat(resultToJsonPath(performSites)).containsOnly(
                "$[0]", "$[0]['id']", "$[0]['name']",
                "$[1]", "$[1]['id']", "$[1]['name']",
                "$[2]", "$[2]['id']", "$[2]['name']",
                "$[3]", "$[3]['id']", "$[3]['name']");

        // When connect site
        ResultActions performConnect = mvc.perform(post(SITES_CONNECT_ENDPOINT)
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .header("x-real-ip", "ff39:6773:c03c:48e8:5b49:492a:d198:4b05")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("{ \"siteId\": \"%s\"}", SOME_YOLT_SITE_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.redirectUrl", equalTo("https://yoltbank.sandbox.yolt.io/yoltbank/yolt-test-bank/authorize?redirect_uri=https://yts-credit-scoring-app:8080/yts-credit-scoring-app/callback&state=c0e3d239-f9c7-4de3-8fab-77c0d5042b40")));
        assertThat(resultToJsonPath(performConnect)).containsOnly("$['redirectUrl']");

        // When bank consent
        ResultActions performUserSite = mvc.perform(post(USER_SITE_ENDPOINT)
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .header("x-real-ip", "ff39:6773:c03c:48e8:5b49:492a:d198:4b05")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("{ \"url\": \"%s\"}", SOME_YOLT_REDIRECT_BACK_URL)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activityId", equalTo("ffa198a7-dc92-4aad-9237-95c0a045091c")));
        assertThat(resultToJsonPath(performUserSite)).containsOnly("$['activityId']", "$['redirectUrl']", "$['dataFetchFailureReason']");

        // When list accounts
        MockHttpServletResponse accountsReponse;
        do {
            accountsReponse = mvc.perform(get(USER_ACCOUNTS_ENDPOINT)
                            .header(HttpHeaders.AUTHORIZATION, token))
                    .andReturn()
                    .getResponse();

        } while (accountsReponse.getStatus() == HttpStatus.ACCEPTED.value());

        // When select account
        ResultActions performAccountSelect = mvc.perform(post(USER_ACCOUNTS_SELECT_ENDPOINT)
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":\"109d740d-2932-4916-a62d-22e363e34dc1\"}"))
                .andExpect(status().isAccepted());
        assertThat(resultToJsonPath(performAccountSelect)).isNull();

        MockHttpServletResponse reportOverviewResponse;
        do {
            final ResultActions performCachflowOverview = mvc.perform(get(CASHFLOW_OVERVIEW_FOR_USER)
                    .header(HttpHeaders.AUTHORIZATION, token));
            reportOverviewResponse = performCachflowOverview
                    .andReturn()
                    .getResponse();
            if (reportOverviewResponse.getStatus() != HttpStatus.ACCEPTED.value())
                break;

            assertThat(resultToJsonPath(performCachflowOverview)).isNull();
        } while (true);

        // When report view
        ResultActions performReportOverview = mvc.perform(get(CASHFLOW_OVERVIEW_FOR_USER)
                        .header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userEmail", equalTo("user@email.com")))
                .andExpect(jsonPath("$.report.currency", equalTo("EUR")))
                .andExpect(jsonPath("$.report.iban", equalTo("NL05INGB1234567890")))
                .andExpect(jsonPath("$.report.creditLimit", equalTo("-5000.05")))
                .andExpect(jsonPath("$.report.accountHolder", equalTo("Account Holder")))
                .andExpect(jsonPath("$.additionalTextReport", equalTo(SOME_CLIENT_ADDITIONAL_TEXT)))
                .andExpect(jsonPath("$.report.lastDataFetchTime", equalTo("2020-12-21T08:57:52Z")))
                .andExpect(jsonPath("$.report.newestTransactionDate", equalTo("2020-12-21")))
                .andExpect(jsonPath("$.report.oldestTransactionDate", equalTo("2019-06-21")));

        assertThat(resultToJsonPath(performReportOverview)).containsOnly(
                "$['userEmail']",
                "$['report']",
                "$['additionalTextReport']",
                "$['report']['userId']",
                "$['report']['iban']",
                "$['report']['bban']",
                "$['report']['maskedPan']",
                "$['report']['sortCodeAccountNumber']",
                "$['report']['initialBalance']",
                "$['report']['lastDataFetchTime']",
                "$['report']['newestTransactionDate']",
                "$['report']['oldestTransactionDate']",
                "$['report']['currency']",
                "$['report']['creditLimit']",
                "$['report']['accountHolder']");
        // When confirm report
        mvc.perform(post(creditReportShareEndpoint)
                        .header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.redirectUrl", equalTo(SOME_CLIENT_REDIRECT_URL+"?userId="+userId+"&status="+creditReportShareStatus)));

        verify(1, deleteRequestedFor(urlMatching("^/v1/users/[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}$")));
    }

    private static Stream<Arguments> testArguments() {
        return Stream.of(
                Arguments.of(CASHFLOW_OVERVIEW_CONFIRM, "confirm"),
                Arguments.of(CASHFLOW_OVERVIEW_REFUSE, "refuse")
        );
    }

    private List<String> resultToJsonPath(ResultActions performConsent) throws UnsupportedEncodingException {
        String contentAsString = performConsent.andReturn().getResponse().getContentAsString();
        return !Objects.equals(contentAsString, "") ? using(conf).parse(contentAsString).read("$..*", List.class) : null;
    }

}
