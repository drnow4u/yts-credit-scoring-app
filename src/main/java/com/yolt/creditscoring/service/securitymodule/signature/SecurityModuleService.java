package com.yolt.creditscoring.service.securitymodule.signature;

import com.yolt.creditscoring.configuration.ClockConfig;
import com.yolt.creditscoring.service.securitymodule.semaevent.SemaEventService;
import com.yolt.creditscoring.service.securitymodule.vault.VaultSecretKeyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@Slf4j
@Service
public class SecurityModuleService {

    private static final Clock clock = ClockConfig.getClock();

    private final PublicKeyRepository publicKeyRepository;
    private final VaultSecretKeyService vaultSecretKeyService;
    private final SemaEventService semaEventService;

    public SecurityModuleService(PublicKeyRepository publicKeyRepository,
                                 VaultSecretKeyService vaultSecretKeyService,
                                 SemaEventService semaEventService) {
        this.publicKeyRepository = publicKeyRepository;
        this.vaultSecretKeyService = vaultSecretKeyService;
        this.semaEventService = semaEventService;
    }

    @PostConstruct
    public void init() {
        final UUID reportSignKeyId = vaultSecretKeyService.getReportSignKeyId();
        final UUID jwtSignKeyId = vaultSecretKeyService.getJwtSignKeyId();

        Stream.of(Map.entry(reportSignKeyId, vaultSecretKeyService.getReportSignPublicKey().getEncoded()),
                Map.entry(jwtSignKeyId, vaultSecretKeyService.getJwtSigningPublicKey().getEncoded())).forEach(entry -> {
            final Optional<PublicKeyEntity> publicKey = publicKeyRepository.findById(entry.getKey());
            if (publicKey.isPresent()) {
                checkIfPublicKeyInDatabaseIsTheSameAsInVault(entry.getKey(), publicKey.get(), entry.getValue());
            } else {
                savePublicKeyEntity(entry.getKey(), entry.getValue());
            }
        });
    }

    private void checkIfPublicKeyInDatabaseIsTheSameAsInVault(UUID keyId, PublicKeyEntity publicKeyEntity, byte[] encodedPublicKeyFromVault) {
        if (!Arrays.equals(publicKeyEntity.getPublicKey(), encodedPublicKeyFromVault)) {
            semaEventService.logIncorrectSignaturePublicKey(keyId);
        }
    }

    private void savePublicKeyEntity(UUID keyId, byte[] publicKey) {
        final PublicKeyEntity publicKeyEntity = new PublicKeyEntity();
        publicKeyEntity.setKid(keyId);
        publicKeyEntity.setCreatedDate(OffsetDateTime.now(clock));
        publicKeyEntity.setPublicKey(publicKey);

        publicKeyRepository.save(publicKeyEntity);
        log.info("New public key saved in database with KID: {}", keyId);
    }

    public PublicKey getPublicKeyByKeyId(@NotNull UUID keyId) {

        final Optional<PublicKey> publicKey = publicKeyRepository.findById(keyId)
                .map(PublicKeyEntity::getPublicKey)
                .map(this::deserializePublicKey);

        if (publicKey.isEmpty()) {
            log.error("Can't find credit report signing public key with ID: {}", keyId);
            throw new RuntimeException("Can't find credit report signing public key with ID:" + keyId);
        }

        return publicKey.get();
    }

    private PublicKey deserializePublicKey(byte[] encodedKey) {
        try {
            X509EncodedKeySpec encodedKeySpec = new X509EncodedKeySpec(encodedKey);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(encodedKeySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            log.error("Signature public key deserialization problem", e);
            throw new SignatureDeserializationException("Signature public key deserialization problem", e);
        }
    }
}
