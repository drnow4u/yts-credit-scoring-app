package com.yolt.creditscoring.service.yoltapi.configuration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.UUID;

@Getter
@AllArgsConstructor
@ConstructorBinding
@ConfigurationProperties(prefix = "yolt.yolt-api")
public class YoltApiProperties {

    @NotEmpty
    private final String baseUrl;

    @NotEmpty
    private final String trustStoreLocation;

    @NotEmpty
    private final String trustStorePassword;

    @NotNull
    private final UUID clientId;

    @NotNull
    private final UUID requestTokenPublicKeyId;

    @NotNull
    private final UUID redirectUrlId;

    @NotNull
    private final Boolean proxyEnabled;

    @NotNull
    private final Boolean vaultEnabled;
}
