package com.yolt.creditscoring.service.estimate.provider;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.yolt.creditscoring.service.estimate.provider.dto.EstimatePDRequestDTO;
import com.yolt.creditscoring.service.estimate.provider.dto.EstimateProbabilityOfDefaultDTO;
import com.yolt.creditscoring.service.estimate.provider.exception.EstimateAPIException;
import com.yolt.creditscoring.service.estimate.provider.exception.NotEnoughTransactionDataException;
import com.yolt.creditscoring.service.securitymodule.vault.VaultSecretKeyService;
import com.yolt.creditscoring.service.yoltapi.exception.FetchDataException;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.transport.ProxyProvider;

import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManagerFactory;
import javax.validation.Valid;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Base64;

@Slf4j
@Service
@Validated
public class EstimateHttpClient {

    private static final Base64.Encoder ENCODER = Base64.getEncoder();
    private final VaultSecretKeyService vaultSecretKeyService;
    private final String baseUrl;
    private final boolean isIspProxyHostEnabled;
    private final String ispProxyHost;
    private final Integer ispProxyPort;
    private TrustOnFirstUseManagerFactory trustManagerFactory;

    public EstimateHttpClient(@Value("${yolt.estimate-api.base-url}") String baseUrl,
                              @Value("${yolt.estimate-api.proxy-enabled}") boolean isIspProxyHostEnabled,
                              @Value("${isp.proxy.host}") String ispProxyHost,
                              @Value("${isp.proxy.port}") Integer ispProxyPort,
                              VaultSecretKeyService vaultSecretKeyService,
                              EstimateSemaEventService estimateSemaEventService) {
        this.isIspProxyHostEnabled = isIspProxyHostEnabled;
        this.ispProxyHost = ispProxyHost;
        this.ispProxyPort = ispProxyPort;
        this.vaultSecretKeyService = vaultSecretKeyService;
        this.baseUrl = baseUrl;
        try {
            trustManagerFactory = new TrustOnFirstUseManagerFactory(createTrustManagerFactory(vaultSecretKeyService.getTrustKeyStore()), estimateSemaEventService);
        } catch (NoSuchAlgorithmException e) {
            log.error("Missing algorithm for truststore");
        } catch (KeyStoreException e) {
            log.error("Can't load truststore");
        }
    }

    public @Valid EstimateProbabilityOfDefaultDTO getPDScoreForGivenAccount(EstimatePDRequestDTO requestBody) throws NoSuchAlgorithmException, KeyStoreException, IOException {
        WebClient webClient = getWebClient();

        ClientResponse clientResponse = webClient.post()
                .uri("/credit-score")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, "Basic " +
                        ENCODER.encodeToString((vaultSecretKeyService.getEstimateApiUser() + ":" +
                                vaultSecretKeyService.getEstimateApiPassword()).getBytes()))
                .body(BodyInserters.fromValue(requestBody))
                .exchange()
                .block();

        if (clientResponse == null) {
            log.warn("Missing client response");
            throw new EstimateAPIException("Missing client response");
        }

        if (clientResponse.statusCode().isError()) {
            String responseBody = StringUtils.defaultString(clientResponse.bodyToMono(String.class).block(), "");
            HttpStatus responseStatus = clientResponse.statusCode();

            log.warn("Error when fetching PD for the report, status: {}", responseStatus); //NOSHERIFF

            if (responseStatus.equals(HttpStatus.BAD_REQUEST) && responseBody.contains("Not enough transactions. Please provide six full months of transactions.")) {
                throw new NotEnoughTransactionDataException(responseBody);
            }

            throw new EstimateAPIException(responseStatus.toString());
        }

        ResponseEntity<String> responseEntity = clientResponse.toEntity(String.class).block();

        if (responseEntity == null) {
            log.warn("Missing response entity");
            throw new EstimateAPIException("Missing response entity");
        }

        if (!MediaType.APPLICATION_JSON.equals(responseEntity.getHeaders().getContentType())) {
            throw new FetchDataException(String.format("Wrong content type %s when fetching PD report. Expected to be %s",
                    responseEntity.getHeaders().getContentType(),
                    MediaType.APPLICATION_JSON
            ));
        }

        log.info("Report fetched from Estimate");
        return objectMapper().readValue(responseEntity.getBody(), EstimateProbabilityOfDefaultDTO.class);
    }

    private WebClient getWebClient() throws KeyStoreException, SSLException {

        if (trustManagerFactory == null)
            throw new KeyStoreException("Truststore not configured");

        SslContext sslContext = SslContextBuilder
                .forClient()
                .trustManager(trustManagerFactory)
                .protocols("TLSv1.2")  // TLSv1.3 is required by Yolt Security, but Estimate is not supporting
                .ciphers(CipherSuite.DEFAULT_SUITE)
                .build();

        HttpClient httpClient = HttpClient.create()
                .secure(sslContextSpec -> sslContextSpec.sslContext(sslContext))
                .responseTimeout(Duration.ofMinutes(10))
                .compress(true);

        if (isIspProxyHostEnabled) {
            log.info("Proxy host: {}:{}", ispProxyHost, ispProxyPort);
            httpClient = httpClient.tcpConfiguration(tcpClient -> tcpClient
                    .proxy(proxy -> proxy
                            .type(ProxyProvider.Proxy.HTTP)
                            .host(ispProxyHost)
                            .port(ispProxyPort)));
        }

        ClientHttpConnector connector = new ReactorClientHttpConnector(httpClient);

        return WebClient.builder()
                .baseUrl(baseUrl)
                .clientConnector(connector)
                .build();
    }

    private TrustManagerFactory createTrustManagerFactory(KeyStore trustKeyStore) throws NoSuchAlgorithmException, KeyStoreException {
        var tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(trustKeyStore);
        return tmf;
    }

    private ObjectMapper objectMapper() {
        return new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

}
