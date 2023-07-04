package com.yolt.creditscoring.service.securitymodule.signature;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import com.yolt.creditscoring.common.signature.SignatureCreditScoreReport;
import com.yolt.creditscoring.controller.admin.users.Based64;
import com.yolt.creditscoring.exception.SignatureException;
import com.yolt.creditscoring.service.securitymodule.vault.VaultSecretKeyService;
import com.yolt.creditscoring.utility.json.PathLeaves;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@Validated
public class SignatureService {

    public static final PathLeaves PATH_LEAVES = new PathLeaves();

    private final VaultSecretKeyService secretKeyService;
    private final SecurityModuleService securityModuleService;
    private final ObjectMapper mapper;
    private Signature signature;

    public SignatureService(VaultSecretKeyService secretKeyService, SecurityModuleService securityModuleService, ObjectMapper mapper) throws NoSuchAlgorithmException {
        this.secretKeyService = secretKeyService;
        this.securityModuleService = securityModuleService;
        try {
            this.signature = Signature.getInstance("SHA256withRSA/PSS");
        } catch (NoSuchAlgorithmException e) {
            this.signature = null;
            throw new SignatureException("Could not get instance of Signature: " + e.getMessage());
        }
        this.mapper = mapper;
    }

    /**
     * @return modulus of the latest signing public key
     */
    public PublicKey getPublicKeyModulus() {
        return secretKeyService.getReportSignPublicKey();
    }

    /**
     * @param signatureKeyId key ID of archived public key
     * @return modulus of the signing public key with given signatureKeyId
     */
    public PublicKey getPublicKeyModulus(UUID signatureKeyId) {
        return securityModuleService.getPublicKeyByKeyId(signatureKeyId);
    }

    public @Valid ReportSignature sign(@NonNull SignatureCreditScoreReport report) {

        var privateKey = secretKeyService.getReportSignPrivateKey();

        try {
            signature.initSign(privateKey);

            final List<String> pathLeaves = PATH_LEAVES.apply(mapper.writeValueAsString(report));

            final String digest = digest(report, pathLeaves);

            this.signature.update(digest.getBytes()); //MAPPER is configured to serialize only not empty fields.

            return ReportSignature.builder()
                    .signature(Based64.of(signature.sign()))
                    .keyId(secretKeyService.getReportSignKeyId())
                    .jsonPaths(pathLeaves)
                    .build();
        } catch (JsonProcessingException | InvalidKeyException | java.security.SignatureException e) {
            throw new SignatureException("Error calculating Signature");
        }
    }

    public boolean verify(@NotNull SignatureCreditScoreReport report, @NotNull ReportSignature reportSignature) {
        var publicKey = securityModuleService.getPublicKeyByKeyId(reportSignature.getKeyId());

        if (publicKey == null) {
            log.error("verify: public key is null, can't continue");
            throw new SignatureException("Public key is null");
        }
        try {
            this.signature.initVerify(publicKey);

            final String digest = digest(report, reportSignature.getJsonPaths());

            this.signature.update(digest.getBytes()); //MAPPER is configured to serialize only not empty fields.

            return this.signature.verify(reportSignature.getSignature().toBytes());
        } catch (InvalidKeyException | java.security.SignatureException | JsonProcessingException e) {
            throw new SignatureException("Error verifying Signature");
        }
    }

    private String digest(SignatureCreditScoreReport report, List<String> pathLeaves) throws JsonProcessingException {
        ReadContext ctx = JsonPath.parse(mapper.writeValueAsString(report));
        StringBuilder digestBuilder = new StringBuilder();

        for (String path : pathLeaves) {
            Object value = ctx.read(path);
            digestBuilder.append(value.toString());
            digestBuilder.append(";");
        }
        return digestBuilder.toString();
    }
}
