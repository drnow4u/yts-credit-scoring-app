package com.yolt.creditscoring.configuration.security.admin;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;

/**
 * On test environments we allow the configuration of microsoft identifiers that can be CFA Admins.
 * This is because we only have 1 security group, and 1 microsoft tenant in which the roles are registered. So we can only
 * identify users that are CFA Admins on production.
 *
 * This is only for testing purposes. The application will fail to startup if anything is configured on an environment
 * that is not a testing environment.
 */
@Getter
@AllArgsConstructor
@ConstructorBinding
@ConfigurationProperties(prefix = "credit-scoring.test-admins")
public class TestCfaAdminProperties {

    private final List<String> microsoftIds;

    @Value("${environment}")
    private final String environment;

    @PostConstruct
    public void validate() {
        if (getMicrosoftIds().isEmpty()) {
            return;
        }
        // This should only be applicable on test environments. (whitelist)
        if (environment.equals("local") ||
                environment.startsWith("team") ||
                environment.equals("yfb-acc") ||
                environment.equals("performance")) {
            return;
        }
        throw new RuntimeException("cfa test admins are configured on an environment where it is not allowed! Fix the configuration and restart the application");
    }
    public List<String> getMicrosoftIds() {
        return microsoftIds != null ? microsoftIds : Collections.emptyList();
    }
}
