package com.yolt.creditscoring.configuration;

import com.yolt.creditscoring.service.securitymodule.vault.VaultSecretKeyService;
import lombok.extern.slf4j.Slf4j;
import nl.ing.lovebird.secretspipeline.VaultKeys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

@Slf4j
@Configuration
public class VaultSecretConfiguration {

    @Bean
    public VaultSecretKeyService createVaultSecretKeyService(VaultKeys vaultKeys,
                                                             @Value("${credit-scoring.trust-store-location}") String trustStoreLocation,
                                                             @Value("${credit-scoring.trust-store-password}") String password) throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException {
        log.info("Using VaultSecretKeyService");
        return new VaultSecretKeyService(vaultKeys, trustStoreLocation, password);
    }
}
