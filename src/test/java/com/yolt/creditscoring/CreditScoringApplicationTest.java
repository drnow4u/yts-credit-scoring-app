package com.yolt.creditscoring;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
class CreditScoringApplicationTest {

    @Value("${local.management.port}")
    private int managementPort;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void applicationStartsUp() {
        HttpStatus statusCode = restTemplate.getForEntity("http://localhost:" + managementPort + "/actuator/info", String.class).getStatusCode();
        assertThat(statusCode).isEqualTo(HttpStatus.OK);
    }

}
