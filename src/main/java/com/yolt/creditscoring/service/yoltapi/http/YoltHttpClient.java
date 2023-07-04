package com.yolt.creditscoring.service.yoltapi.http;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.yolt.creditscoring.configuration.ClockConfig;
import com.yolt.creditscoring.service.yoltapi.exception.FetchDataException;
import com.yolt.creditscoring.service.yoltapi.http.model.*;
import com.yolt.creditscoring.service.yoltapi.webclient.ClientAuthenticationMeans;
import com.yolt.creditscoring.utility.tracing.TraceIdSupplier;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.lang.JoseException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Clock;
import java.time.LocalDate;
import java.time.Period;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

@Slf4j
@AllArgsConstructor
public class YoltHttpClient {

    public static final String REQUEST_TRACE_ID_HEADER_NAME = "request_trace_id";
    private static final Clock clock = ClockConfig.getClock();

    private final WebClient webClient;
    private final boolean oneOffAisUser;
    private final Supplier<String> traceIdSupplier = new TraceIdSupplier();

    public AccessTokenResponse getAccessToken(ClientAuthenticationMeans authenticationMeans) throws JoseException {
        log.info("Getting Access Token");

        JwtClaims claims = new JwtClaims();
        claims.setIssuer(authenticationMeans.getClientId().toString());
        claims.setIssuedAtToNow();
        claims.setJwtId(UUID.randomUUID().toString());

        JsonWebSignature jws = new JsonWebSignature();
        jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.RSA_USING_SHA512);
        jws.setPayload(claims.toJson());
        jws.setKeyIdHeaderValue(authenticationMeans.getRequestTokenPublicKeyId().toString());
        jws.setKey(authenticationMeans.getSigningPrivateKey());

        String requestToken = jws.getCompactSerialization();
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>(2);
        body.add("grant_type", "client_credentials");
        body.add("request_token", requestToken);

        ClientResponse clientResponse = webClient.post()
                .uri("/v1/tokens")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .header(REQUEST_TRACE_ID_HEADER_NAME, traceIdSupplier.get())
                .body(BodyInserters.fromFormData(body))
                .exchange()
                .block();

        return fetchResponse(clientResponse, AccessTokenResponse.class);
    }

    public ClientSiteEntity[] getClientSite(ClientAuthenticationMeans authenticationMeans, String siteTags) {
        StringUtils.isNotBlank(authenticationMeans.getAccessToken());
        log.info("Get client sites");

        Map<String, String> uriVariables = new HashMap<>();
        uriVariables.put("siteTags", siteTags);

        ClientResponse clientResponse = webClient.get()
                .uri("/v2/sites?tag={siteTags}", uriVariables)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authenticationMeans.getAccessToken())
                .header(REQUEST_TRACE_ID_HEADER_NAME, traceIdSupplier.get())
                .exchange()
                .block();

        return fetchResponse(clientResponse, ClientSiteEntity[].class);
    }

    public ClientUser createUser(ClientAuthenticationMeans authenticationMeans) {

        ClientResponse clientResponse;
        if (oneOffAisUser) {
            clientResponse = webClient.post()
                    .uri("/v5/users")
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + authenticationMeans.getAccessToken())
                    .header(REQUEST_TRACE_ID_HEADER_NAME, traceIdSupplier.get())
                    .body(BodyInserters.fromValue(new CreateOneOffAISUserRequest()))
                    .exchange()
                    .block();
        } else {
            clientResponse = webClient.post()
                    .uri("/v2/users")
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + authenticationMeans.getAccessToken())
                    .header(REQUEST_TRACE_ID_HEADER_NAME, traceIdSupplier.get())
                    .exchange()
                    .block();
        }

        ClientUser clientUser = fetchResponse(clientResponse, ClientUser.class);
        log.info("Created client user '{}' in yolt.", clientUser.getId());
        return clientUser;
    }

    public LoginStep requestUserConsent(ClientAuthenticationMeans authenticationMeans,
                                        UserConsentParams userConsentParams) {
        log.info("Requesting user consent page");

        Map<String, String> uriVariables = new HashMap<>();
        uriVariables.put("userId", userConsentParams.getUserId().toString());
        uriVariables.put("siteId", userConsentParams.getSiteId().toString());
        uriVariables.put("redirectUrlId", authenticationMeans.getRedirectUrlId().toString());

        ClientResponse clientResponse = webClient.post()
                .uri("/v1/users/{userId}/connect?site={siteId}&redirectUrlId={redirectUrlId}", uriVariables)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authenticationMeans.getAccessToken())
                .header("PSU-IP-Address", userConsentParams.getPsuIpAddress())
                .header(REQUEST_TRACE_ID_HEADER_NAME, traceIdSupplier.get())
                .exchange()
                .block();

        return fetchResponse(clientResponse, LoginStep.class);
    }

    public LoginFormResponse createUserSite(ClientAuthenticationMeans authenticationMeans,
                                            UUID yoltUserId,
                                            String redirectUrl,
                                            String userIpAddress) {
        log.info("Creating user site");

        Map<String, String> uriVariables = new HashMap<>();
        uriVariables.put("userId", yoltUserId.toString());

        CreateUserSiteUrl createUserSite = new CreateUserSiteUrl(redirectUrl);

        ClientResponse clientResponse = webClient.post()
                .uri("/v1/users/{userId}/user-sites", uriVariables)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authenticationMeans.getAccessToken())
                .header("PSU-IP-Address", userIpAddress)
                .header(REQUEST_TRACE_ID_HEADER_NAME, traceIdSupplier.get())
                .body(BodyInserters.fromValue(createUserSite))
                .exchange()
                .block();

        return fetchResponse(clientResponse, LoginFormResponse.class);
    }

    public LoginFormResponse createUserSiteAfterDynamicFlow(ClientAuthenticationMeans authenticationMeans,
                                                            UUID yoltUserId,
                                                            CreateUserSiteForm createUserSiteForm,
                                                            String userIpAddress) {
        log.info("Sending dynamic flow");

        Map<String, String> uriVariables = new HashMap<>();
        uriVariables.put("userId", yoltUserId.toString());

        ClientResponse clientResponse = webClient.post()
                .uri("/v1/users/{userId}/user-sites", uriVariables)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authenticationMeans.getAccessToken())
                .header("PSU-IP-Address", userIpAddress)
                .header(REQUEST_TRACE_ID_HEADER_NAME, traceIdSupplier.get())
                .body(BodyInserters.fromValue(createUserSiteForm))
                .exchange()
                .block();

        return fetchResponse(clientResponse, LoginFormResponse.class);
    }

    public Void removeUser(ClientAuthenticationMeans authenticationMeans, @NonNull UUID yoltUserId) {
        log.info("Removing Yolt user");

        Map<String, String> uriVariables = new HashMap<>();
        uriVariables.put("userId", yoltUserId.toString());

        ClientResponse clientResponse = webClient.delete()
                .uri("/v1/users/{userId}", uriVariables)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authenticationMeans.getAccessToken())
                .header(REQUEST_TRACE_ID_HEADER_NAME, traceIdSupplier.get())
                .exchange()
                .block();

        if (clientResponse.statusCode().isError()) {
            log.warn("Delete user-site error: {}", clientResponse.statusCode());
            throw new FetchDataException(clientResponse.statusCode().toString());
        }

        return clientResponse
                .bodyToMono(Void.class)
                .block();
    }

    public UserSite getUserSiteStatus(ClientAuthenticationMeans authenticationMeans,
                                      UUID yoltUserId,
                                      UUID yoltUserSiteId) {

        Map<String, String> uriVariables = new HashMap<>();
        uriVariables.put("userId", yoltUserId.toString());
        uriVariables.put("userSiteId", yoltUserSiteId.toString());

        ClientResponse clientResponse = webClient.get()
                .uri("/v1/users/{userId}/user-sites/{userSiteId}", uriVariables)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authenticationMeans.getAccessToken())
                .header(REQUEST_TRACE_ID_HEADER_NAME, traceIdSupplier.get())
                .exchange()
                .block();

        return fetchResponse(clientResponse, UserSite.class);
    }

    public ActivitiesDTO getUserActivities(ClientAuthenticationMeans authenticationMeans,
                                           UUID yoltUserId) {

        Map<String, String> uriVariables = new HashMap<>();
        uriVariables.put("userId", yoltUserId.toString());

        ClientResponse clientResponse = webClient.get()
                .uri("/v1/users/{userId}/activities", uriVariables)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authenticationMeans.getAccessToken())
                .header(REQUEST_TRACE_ID_HEADER_NAME, traceIdSupplier.get())
                .exchange()
                .block();

        return fetchResponse(clientResponse, ActivitiesDTO.class);
    }

    public AccountDTO[] accounts(ClientAuthenticationMeans authenticationMeans,
                                 UUID yoltUserId) {
        log.info("Fetching accounts");

        Map<String, String> uriVariables = new HashMap<>();
        uriVariables.put("userId", yoltUserId.toString());

        ClientResponse clientResponse = webClient.get()
                .uri("/v1/users/{userId}/accounts", uriVariables)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authenticationMeans.getAccessToken())
                .header(REQUEST_TRACE_ID_HEADER_NAME, traceIdSupplier.get())
                .exchange()
                .block();

        return fetchResponse(clientResponse, AccountDTO[].class);
    }

    public TransactionsPageDTO getTransactions(ClientAuthenticationMeans authenticationMeans,
                                               UUID yoltUserId,
                                               UUID accountId) {
        return getTransactions(authenticationMeans, yoltUserId, accountId, StringUtils.EMPTY);
    }

    public TransactionsPageDTO getTransactions(ClientAuthenticationMeans authenticationMeans,
                                               UUID yoltUserId,
                                               UUID accountId,
                                               String next) {
        String transactionsUrl = "/v1/users/{userId}/transactions?accountIds={accountId}&dateInterval={dateInterval}";

        Map<String, String> uriVariables = new HashMap<>();
        uriVariables.put("userId", yoltUserId.toString());
        uriVariables.put("accountId", accountId.toString());
        uriVariables.put("dateInterval", Period.ofMonths(18) + "/" + LocalDate.now(clock));

        if (StringUtils.isNotEmpty(next)) {
            transactionsUrl += "&next={next}";
            uriVariables.put("next", next);
        } else {
            log.info("Fetching last transactions");
        }

        ClientResponse clientResponse = webClient.get()
                .uri(transactionsUrl, uriVariables)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authenticationMeans.getAccessToken())
                .header(REQUEST_TRACE_ID_HEADER_NAME, traceIdSupplier.get())
                .exchange()
                .block();

        return fetchResponse(clientResponse, TransactionsPageDTO.class);
    }

    public TransactionCyclesDTO getCycleTransactions(@NonNull ClientAuthenticationMeans authenticationMeans,
                                                     @NonNull UUID yoltUserId) {
        String transactionsUrl = "/v1/users/{userId}/transaction-cycles";

        Map<String, String> uriVariables = new HashMap<>();
        uriVariables.put("userId", yoltUserId.toString());

        ClientResponse clientResponse = webClient.get()
                .uri(transactionsUrl, uriVariables)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authenticationMeans.getAccessToken())
                .header(REQUEST_TRACE_ID_HEADER_NAME, traceIdSupplier.get())
                .exchange()
                .block();

        return fetchResponse(clientResponse, TransactionCyclesDTO.class);
    }

    private <T> T fetchResponse(ClientResponse clientResponse,
                                Class<T> valueType) {

        if (clientResponse.statusCode().isError()) {
            log.error("Error when fetching {}, status: {}", valueType.getSimpleName(), clientResponse.statusCode());
            throw new FetchDataException(clientResponse.statusCode().toString());
        }

        ResponseEntity<String> responseEntity = clientResponse.toEntity(String.class).block();

        if (!MediaType.APPLICATION_JSON.equals(responseEntity.getHeaders().getContentType())) {
            throw new FetchDataException(String.format("Wrong content type %s when fetching %s. Expected to be %s",
                    responseEntity.getHeaders().getContentType(),
                    valueType.getSimpleName(),
                    MediaType.APPLICATION_JSON
            ));
        }

        try {
            return objectMapper().readValue(responseEntity.getBody(), valueType);
        } catch (JsonProcessingException e) {
            throw new FetchDataException("Unable to fetch response because of " + e.getMessage(), e);
        }
    }

    public static ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper;
    }
}
