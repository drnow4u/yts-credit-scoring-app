package com.yolt.creditscoring.service.yoltapi.webclient;

import com.yolt.creditscoring.service.securitymodule.vault.VaultSecretKeyService;
import com.yolt.creditscoring.service.yoltapi.http.YoltHttpClient;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import javax.net.ssl.TrustManagerFactory;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Objects;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class YoltApiConfiguration {

    @Value("${yolt.yolt-api.base-url}")
    private final String baseUrl;

    private final VaultSecretKeyService secretKeyService;
    /**
     * Unfortunately, this functionality is kept in the application so CFA can also be run locally.
     * This property is only true if you want to run this application from local to the real yolt-api on the public domain.
     * Note that on PRD/team-envs, CFA points to the client-proxy directly. It bypasses the ingress that enforces TLS-MA
     */
    @Value("${yolt.yolt-api.mtls.enabled}")
    private final boolean useMtls;

    /**
     * This seems to be a hack in order to be able to run integration tests. Normally, we take the jvm truststore,
     * otherwise, a configured truststore in /resources folder.
     * This should be refactored at some point.
     */
    @Value("${yolt.yolt-api.jvm-truststore}")
    private final boolean jvmTruststore;

    /**
     * This is due to deploy CFA to sandbox environment problem.
     * For all DTA and PRD it is configured to true, but for sandbox it has to be false.
     * It can be removed when CFA will become the first class citizen of Yolt API.
     */
    @Value("${yolt.yolt-api.one-off-ais-user}")
    private final boolean oneOffAisUser;

    @Bean
    public YoltHttpClient yoltHttpClient() throws Exception {
        return new YoltHttpClient(yoltWebClient(), oneOffAisUser);
    }

    private WebClient yoltWebClient() throws Exception {
        log.debug("WebClient configured for baseUrl: {}.", baseUrl);

        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        if (jvmTruststore) {
            trustManagerFactory.init((KeyStore) null);
        } else {
            trustManagerFactory.init(secretKeyService.getTrustKeyStore());
        }
        SslContextBuilder sslContextBuilder = SslContextBuilder
                .forClient()
                .trustManager(trustManagerFactory);

        if (useMtls) {
            X509Certificate certificate = secretKeyService.getTransportCertificate();
            PrivateKey transportPrivateKey = secretKeyService.getTransportPrivateKey();
                    sslContextBuilder.keyManager(
                            Objects.requireNonNull(transportPrivateKey, "tls MA is enabled, but the private key is not provided!"),
                            Objects.requireNonNull(certificate, "tls MA is enabled, but the certificate is not provided!")
                    );
        }

        SslContext sslContext = sslContextBuilder.build();


        HttpClient httpClient = HttpClient.create()
                .secure(sslContextSpec -> sslContextSpec.sslContext(sslContext));


        ClientHttpConnector connector = new ReactorClientHttpConnector(httpClient);

        return WebClient.builder()
                .baseUrl(baseUrl)
                .clientConnector(connector)
                .build();
    }
}
