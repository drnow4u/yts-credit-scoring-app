package com.yolt.creditscoring.service.securitymodule.vault;

import lombok.extern.slf4j.Slf4j;
import nl.ing.lovebird.secretspipeline.VaultKeys;
import org.jose4j.jwk.RsaJsonWebKey;

import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.UUID;

@Slf4j
public class VaultSecretKeyService {
    /**
     * Is only support one client
     */
    public static final String TRANSPORT_VAULT_KEY_NAME = "yts-app-tls-key";
    public static final String TRANSPORT_VAULT_CERT_NAME = "yts-app-tls-cert";
    public static final String SIGNING_VAULT_KEY_NAME = "yts-app-signing-key";
    public static final String JWT_SIGNING_VAULT_KEY_NAME = "yts-app-jwt-sign";
    public static final String JWT_ENCRYPTION_VAULT_KEY_NAME = "yts-app-jwt-encr";
    public static final String REPORT_SIGN_PRIVATE_KEY = "yts-app-rep-sign-jwks";
    public static final String ESTIMATE_API_USER = "estimate-api-user";
    public static final String ESTIMATE_API_PASSWORD = "estimate-api-password";
    private final VaultKeys vaultKeys;
    private final KeyStore trustStore;

    public VaultSecretKeyService(VaultKeys vaultKeys,
                                 String trustStoreLocation,
                                 String password) throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException {
        this.vaultKeys = vaultKeys;
        trustStore = loadTrustStore(trustStoreLocation, password);
    }

    /**
     * Unfortunately, this functionality is kept in the application so CFA can also be run locally.
     * This will only resolve to a private key when configured. That will only be from a local environment for now.
     * Note that on PRD/team-envs, CFA points to the client-proxy directly. It bypasses the ingress that enforces TLS-MA
     */
    public PrivateKey getTransportPrivateKey() {
        return vaultKeys.getPrivateKey(TRANSPORT_VAULT_KEY_NAME).getKey();
    }

    /**
     * Unfortunately, this functionality is kept in the application so CFA can also be run locally.
     * This will only resolve to a certificate  when configured. That will only be from a local environment for now.
     * Note that on PRD/team-envs, CFA points to the client-proxy directly. It bypasses the ingress that enforces TLS-MA
     */
    public X509Certificate getTransportCertificate() {
        return (X509Certificate) vaultKeys.getCertificate(TRANSPORT_VAULT_CERT_NAME);
    }

    public KeyStore getTrustKeyStore() {
        return trustStore;
    }

    public PrivateKey getSigningPrivateKey() {
        return vaultKeys.getPrivateKey(SIGNING_VAULT_KEY_NAME).getKey();
    }

    public PrivateKey getReportSignPrivateKey() {
        // Not returned vaultKeys.getJsonWebKey(REPORT_SIGN_PRIVATE_KEY) because we send public key to front-end
        // and don't want to compromise private key by mistake in code.
        return ((RsaJsonWebKey) vaultKeys.getJsonWebKey(REPORT_SIGN_PRIVATE_KEY)).getRsaPrivateKey();
    }

    public PublicKey getReportSignPublicKey() {
        // Not returned vaultKeys.getJsonWebKey(REPORT_SIGN_PRIVATE_KEY) because we send public key to front-end
        // and don't want to compromise private key by mistake in code.
        return ((RsaJsonWebKey) vaultKeys.getJsonWebKey(REPORT_SIGN_PRIVATE_KEY)).getRsaPublicKey();
    }

    public UUID getReportSignKeyId() {
        return UUID.fromString(vaultKeys.getJsonWebKey(REPORT_SIGN_PRIVATE_KEY).getKeyId());
    }

    public UUID getJwtSignKeyId() {
        return UUID.fromString(vaultKeys.getJsonWebKey(JWT_SIGNING_VAULT_KEY_NAME).getKeyId());
    }

    public PrivateKey getJwtSigningPrivateKey() {
        return ((RsaJsonWebKey) vaultKeys.getJsonWebKey(JWT_SIGNING_VAULT_KEY_NAME)).getRsaPrivateKey();
    }

    public PublicKey getJwtSigningPublicKey() {
        return ((RsaJsonWebKey) vaultKeys.getJsonWebKey(JWT_SIGNING_VAULT_KEY_NAME)).getRsaPublicKey();
    }

    public String getEstimateApiUser() {
        return new String(vaultKeys.getPassword(ESTIMATE_API_USER).getEncoded());
    }

    public String getEstimateApiPassword() {
        return new String(vaultKeys.getPassword(ESTIMATE_API_PASSWORD).getEncoded());
    }

    public Key getJwtEncryptionKey() {
        return vaultKeys.getSymmetricKey(JWT_ENCRYPTION_VAULT_KEY_NAME).getKey();
    }

    public static KeyStore loadTrustStore(String filename, String changeit) throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        KeyStore trustKeyStore = KeyStore.getInstance("JKS");
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        try (InputStream inputStream = classLoader.getResourceAsStream(filename)) {

            if (inputStream == null) {
                throw new KeyStoreException(String.format("Truststore '%s' does not exist.", filename));
            }

            trustKeyStore.load(inputStream, changeit.toCharArray());
        }
        return trustKeyStore;
    }
}
