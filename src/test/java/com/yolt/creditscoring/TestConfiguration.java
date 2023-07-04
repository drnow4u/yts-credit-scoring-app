package com.yolt.creditscoring;

import com.github.tomakehurst.wiremock.common.JettySettings;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import com.github.tomakehurst.wiremock.http.AdminRequestHandler;
import com.github.tomakehurst.wiremock.http.HttpServer;
import com.github.tomakehurst.wiremock.http.HttpServerFactory;
import com.github.tomakehurst.wiremock.http.StubRequestHandler;
import com.github.tomakehurst.wiremock.jetty9.JettyHttpServer;
import org.springframework.cloud.contract.wiremock.WireMockConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import wiremock.org.eclipse.jetty.io.NetworkTrafficListener;
import wiremock.org.eclipse.jetty.server.ConnectionFactory;
import wiremock.org.eclipse.jetty.server.ServerConnector;
import wiremock.org.eclipse.jetty.server.SslConnectionFactory;

/**
 * Test configuration. Solely to make some integrationstest 'easier to make'.
 * This is created when splitting of the providers. A lot of providers made use of spring of the application where they were actually used
 * to do some autowiring in integrationtests.
 */
@Configuration
public class TestConfiguration {

    @Bean
    WireMockConfigurationCustomizer optionsCustomizer() {
        return options -> options
                .httpServerFactory(new Pkcs12FriendlyHttpsServerFactory())
                .extensions(new ResponseTemplateTransformer(false))
                .keystoreType("pkcs12")
                .keystorePath("src/test/resources/certificates/fake-keystore.p12")
                .keystorePassword("changeit")
                .keyManagerPassword("changeit");
    }

    /**
     * Needed for SSL handshake in combination with pkcs12 keystore.
     */
    private class Pkcs12FriendlyHttpsServerFactory implements HttpServerFactory {
        @Override
        public HttpServer buildHttpServer(Options options, AdminRequestHandler adminRequestHandler, StubRequestHandler stubRequestHandler) {
            return new JettyHttpServer(
                    options,
                    adminRequestHandler,
                    stubRequestHandler
            ) {
                @Override
                protected ServerConnector createServerConnector(String bindAddress, JettySettings jettySettings, int port, NetworkTrafficListener listener, ConnectionFactory... connectionFactories) {
                    if (port == options.httpsSettings().port()) {
                        SslConnectionFactory sslConnectionFactory = (SslConnectionFactory) connectionFactories[0];
                        sslConnectionFactory.getSslContextFactory().setKeyStorePassword(options.httpsSettings().keyStorePassword());
                        connectionFactories = new ConnectionFactory[]{sslConnectionFactory, connectionFactories[1]};
                    }
                    return super.createServerConnector(bindAddress, jettySettings, port, listener, connectionFactories);
                }
            };

        }
    }

}
