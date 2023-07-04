package com.yolt.creditscoring.configuration;

import lombok.extern.slf4j.Slf4j;
import nl.ing.lovebird.http.YoltProxySelector;
import org.apache.http.impl.conn.SystemDefaultRoutePlanner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.http.SdkHttpClient;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;
import software.amazon.awssdk.services.ses.model.SendEmailResponse;

import java.util.UUID;

@Slf4j
@Configuration
public class AWSEmailConfig {

    @Bean
    @ConditionalOnProperty(value = "credit-scoring.amazon-ses.enabled", havingValue = "true")
    public SesClient amazonSimpleEmailService(@Value("${isp.proxy.host}") final String ispProxyHost,
                                              @Value("${isp.proxy.port}") final Integer ispProxyPort,
                                              AwsCredentialsProvider awsCredentialsProvider) {

        SdkHttpClient httpClient = ApacheHttpClient.builder()
                .httpRoutePlanner(new SystemDefaultRoutePlanner(new YoltProxySelector(ispProxyHost, ispProxyPort)))
                .build();

        return SesClient.builder()
                .region(Region.EU_CENTRAL_1)
                .credentialsProvider(awsCredentialsProvider)
                .httpClient(httpClient)
                .build();
    }

    /**
     * Do not use AWS to send the email, only logs the output.
     * AWS credentials are not required.
     * Should be used only for testing on local machine.
     *
     * @return Stubed SesClient for local testing.
     */
    @Bean
    @ConditionalOnProperty(value = "credit-scoring.amazon-ses.enabled", havingValue = "false")
    public SesClient amazonSimpleEmailServiceLocal() {
        log.warn("Using local configuration for email service");
        return new SesClient() {

            @Override
            public SendEmailResponse sendEmail(SendEmailRequest sendEmailRequest) {
                log.info("Email request send - " + sendEmailRequest.toString()); //NOSHERIFF Used only for local configuration
                return SendEmailResponse.builder()
                        .messageId(UUID.randomUUID().toString())
                        .build();
            }

            @Override
            public String serviceName() {
                return "Local";
            }

            @Override
            public void close() {
                //no-op
            }
        };
    }
}
