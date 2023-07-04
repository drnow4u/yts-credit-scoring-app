package com.yolt.creditscoring;

import com.yolt.creditscoring.configuration.security.admin.TestCfaAdminProperties;
import com.yolt.creditscoring.service.yoltapi.configuration.YoltApiProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;


@EnableConfigurationProperties(value = {YoltApiProperties.class, TestCfaAdminProperties.class})
@SpringBootApplication
public class CreditScoringApplication {

    public static void main(String[] args) {
        SpringApplication.run(CreditScoringApplication.class, args);
    }
}
