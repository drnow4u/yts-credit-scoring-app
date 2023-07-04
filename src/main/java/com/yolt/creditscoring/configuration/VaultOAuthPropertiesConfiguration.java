package com.yolt.creditscoring.configuration;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import nl.ing.lovebird.secretspipeline.VaultKeys;
import nl.ing.lovebird.secretspipeline.VaultKeysReader;
import nl.ing.lovebird.secretspipeline.converters.KeyStoreReader;
import nl.ing.lovebird.secretspipeline.converters.PasswordKeyStoreReader;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;

import java.security.Security;
import java.util.List;
import java.util.Properties;

import static java.util.Collections.singletonList;

@Slf4j
public class VaultOAuthPropertiesConfiguration implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

    private static final String GITHUB_CLIENT_ID = "github-client-id";
    private static final String GITHUB_CLIENT_SECRET = "github-client-secret";
    private static final String GITHUB_OAUTH2_CLIENT_ID_PROPERTY = "spring.security.oauth2.client.registration.github.clientId";
    private static final String GITHUB_OAUTH2_CLIENT_SECRET_PROPERTY = "spring.security.oauth2.client.registration.github.clientSecret";

    private static final String GOOGLE_CLIENT_ID = "google-client-id";
    private static final String GOOGLE_CLIENT_SECRET = "google-client-secret";
    private static final String GOOGLE_OAUTH2_CLIENT_ID_PROPERTY = "spring.security.oauth2.client.registration.google.clientId";
    private static final String GOOGLE_OAUTH2_CLIENT_SECRET_PROPERTY = "spring.security.oauth2.client.registration.google.clientSecret";

    private static final String MICROSOFT_CLIENT_ID = "ms-client-id";
    private static final String MICROSOFT_CLIENT_SECRET = "ms-client-secret";
    private static final String MICROSOFT_OAUTH2_CLIENT_ID_PROPERTY = "spring.security.oauth2.client.registration.microsoft.clientId";
    private static final String MICROSOFT_OAUTH2_CLIENT_SECRET_PROPERTY = "spring.security.oauth2.client.registration.microsoft.clientSecret";

    private static final String APY_OAUTH2_CLIENT_SECRET_MISSING_ERROR = "Unable to find client-secret for oauth2 client registration: ";

    static {
        Security.addProvider(new BouncyCastleProvider()); // Vault need this
    }

    @SneakyThrows
    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        ConfigurableEnvironment environment = event.getEnvironment();
        String secretsLocation = environment.getProperty("yolt.vault.secret.location", "file:/vault/secrets");
        Resource secretsLocationResource = new DefaultResourceLoader().getResource(secretsLocation);

        if (secretsLocationResource.exists()) {
            List<KeyStoreReader> keyStoreReaderList = singletonList(new PasswordKeyStoreReader());
            VaultKeys vaultKeys = new VaultKeysReader(keyStoreReaderList).readFiles(secretsLocationResource.getURI());

            Properties props = new Properties();

            props.put(GITHUB_OAUTH2_CLIENT_ID_PROPERTY, new String(vaultKeys.getPassword(GITHUB_CLIENT_ID).getEncoded()));
            props.put(GITHUB_OAUTH2_CLIENT_SECRET_PROPERTY, new String(vaultKeys.getPassword(GITHUB_CLIENT_SECRET).getEncoded()));

            props.put(GOOGLE_OAUTH2_CLIENT_ID_PROPERTY, new String(vaultKeys.getPassword(GOOGLE_CLIENT_ID).getEncoded()));
            props.put(GOOGLE_OAUTH2_CLIENT_SECRET_PROPERTY, new String(vaultKeys.getPassword(GOOGLE_CLIENT_SECRET).getEncoded()));

            props.put(MICROSOFT_OAUTH2_CLIENT_ID_PROPERTY, new String(vaultKeys.getPassword(MICROSOFT_CLIENT_ID).getEncoded()));
            props.put(MICROSOFT_OAUTH2_CLIENT_SECRET_PROPERTY, new String(vaultKeys.getPassword(MICROSOFT_CLIENT_SECRET).getEncoded()));

            environment.getPropertySources().addFirst(new PropertiesPropertySource("VaultSecretsConfiguration", props));

            log.info("Provisioned {} with the Vault secret '{}'", GITHUB_OAUTH2_CLIENT_SECRET_PROPERTY, GITHUB_CLIENT_SECRET);
            log.info("Provisioned {} with the Vault secret '{}'", GOOGLE_OAUTH2_CLIENT_ID_PROPERTY, GOOGLE_CLIENT_SECRET);
            log.info("Provisioned {} with the Vault secret '{}'", MICROSOFT_OAUTH2_CLIENT_ID_PROPERTY, MICROSOFT_CLIENT_ID);
        } else {
            throw new IllegalStateException(APY_OAUTH2_CLIENT_SECRET_MISSING_ERROR + "Directory for injected vault secrets does not exist: " + secretsLocation);
        }

    }
}
