package com.yolt.creditscoring.service.securitymodule.jwt;

import com.yolt.creditscoring.configuration.security.SecurityRoles;
import com.yolt.creditscoring.configuration.security.admin.OAuth2AdminUser;
import com.yolt.creditscoring.exception.EncryptionException;
import com.yolt.creditscoring.exception.JwtCreationException;
import com.yolt.creditscoring.service.clienttoken.model.ClientTokenPermission;
import com.yolt.creditscoring.service.securitymodule.signature.SecurityModuleService;
import com.yolt.creditscoring.service.securitymodule.vault.VaultSecretKeyService;
import lombok.RequiredArgsConstructor;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.lang.JoseException;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.yolt.creditscoring.configuration.security.admin.AdminClaims.*;

@Service
@RequiredArgsConstructor
public class JwtCreationService {

    public static final int TOKEN_EXPIRATION_TIME_MINUTES = 60;
    public static final Duration API_TOKEN_EXPIRATION_DURATION = Duration.ofDays(180);
    public static final String TOKEN_TYPE_PREFIX = "Bearer";
    public static final String CLIENT_TOKEN_SUBJECT = "CLIENT_TOKEN";

    private final VaultSecretKeyService secretKeyService;
    private final JwtEncryption jwtEncryption;
    private final SecurityModuleService securityModuleService;

    public String createAdminToken(OAuth2AdminUser principal) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put(ROLES, principal.getAuthorities().stream().filter(it -> it.getAuthority().startsWith(SecurityRoles.ROLE_PREFIX)).collect(Collectors.toSet()));
        extraClaims.put(EMAIL, principal.getEmail());
        extraClaims.put(IPDID, principal.getIdpId());
        principal.getClientAdmin().ifPresent(clientAdmin -> {
            extraClaims.put(CLIENT_ID, clientAdmin.getClientId());
            extraClaims.put(ADMIN_ID, clientAdmin.getId());
        });

        return this.createJWTToken(principal.getIdpId(), extraClaims);
    }

    public String createUserToken(String valueForSubjectClaim) {
        return this.createJWTToken(valueForSubjectClaim, Map.of());
    }

    public JwtClientToken createClientToken(List<ClientTokenPermission> permissions) {
        UUID jwtId = UUID.randomUUID();

        JwtClaims claims = new JwtClaims();
        claims.setSubject(CLIENT_TOKEN_SUBJECT);
        claims.setIssuedAtToNow();
        claims.setJwtId(jwtId.toString());
        claims.setExpirationTimeMinutesInTheFuture(API_TOKEN_EXPIRATION_DURATION.toMinutes());
        claims.setClaim("scope", permissions);

        JsonWebSignature jws = new JsonWebSignature();
        jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.RSA_USING_SHA512);
        jws.setPayload(claims.toJson());

        jws.setKey(secretKeyService.getJwtSigningPrivateKey());
        try {
            return new JwtClientToken(jwtId, jwtEncryption.encrypt(jws.getCompactSerialization()), secretKeyService.getJwtSignKeyId());
        } catch (JoseException | EncryptionException e) {
            throw new JwtCreationException("There was an error when creating client JWT");
        }
    }

    private String createJWTToken(String valueForSubjectClaim, Map<String, Object> extraClaims) {
        JwtClaims claims = new JwtClaims();
        claims.setSubject(valueForSubjectClaim);
        claims.setIssuedAtToNow();
        claims.setJwtId(UUID.randomUUID().toString());
        claims.setExpirationTimeMinutesInTheFuture(TOKEN_EXPIRATION_TIME_MINUTES);
        extraClaims.forEach(claims::setClaim);

        JsonWebSignature jws = new JsonWebSignature();
        jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.RSA_USING_SHA512);
        jws.setPayload(claims.toJson());

        jws.setKey(secretKeyService.getJwtSigningPrivateKey());
        try {
            return TOKEN_TYPE_PREFIX + " " + jwtEncryption.encrypt(jws.getCompactSerialization());
        } catch (JoseException | EncryptionException e) {
            throw new JwtCreationException("There was an error when creating JWT for user");
        }
    }

    public String createJWTCookie(OAuth2AuthorizationRequest authorizationRequest) {
        JwtClaims claims = new JwtClaims();
        claims.setSubject("Auth Cookie");
        claims.setIssuedAtToNow();
        claims.setJwtId(UUID.randomUUID().toString());
        claims.setExpirationTimeMinutesInTheFuture(3);

        claims.setClaim("authorizationUri", authorizationRequest.getAuthorizationUri());
        claims.setClaim("authorizationGrantType", authorizationRequest.getGrantType().getValue());
        claims.setClaim("responseType", authorizationRequest.getResponseType().getValue());
        claims.setClaim("clientId", authorizationRequest.getClientId());
        claims.setClaim("redirectUri", authorizationRequest.getRedirectUri());
        claims.setClaim("scopes", authorizationRequest.getScopes());
        claims.setClaim("state", authorizationRequest.getState());
        claims.setClaim("additionalParameters", authorizationRequest.getAdditionalParameters());
        claims.setClaim("authorizationRequestUri", authorizationRequest.getAuthorizationRequestUri());
        claims.setClaim("attributes", authorizationRequest.getAttributes());

        JsonWebSignature jws = new JsonWebSignature();
        jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.RSA_USING_SHA512);
        jws.setPayload(claims.toJson());

        jws.setKey(secretKeyService.getJwtSigningPrivateKey());
        try {
            return jwtEncryption.encrypt(jws.getCompactSerialization());
        } catch (JoseException | EncryptionException e) {
            throw new JwtCreationException("There was an error when creating JWT for oauth authorization");
        }
    }

    public JwtClaims getJwtClaimsFromDecryptedJwtAndPerformValidation(String encryptedJwt) {
        try {
            String decrypt = jwtEncryption.decrypt(encryptedJwt);

            JwtConsumer jwtConsumer = new JwtConsumerBuilder()
                    .setVerificationKey(secretKeyService.getJwtSigningPublicKey())
                    .build();

            return jwtConsumer
                    .process(decrypt)
                    .getJwtClaims();
        } catch (Exception e) {
            throw new JwtCreationException("There was an error decrypting JWT");
        }
    }

    public JwtClaims validateDecryptedJwt(String encryptedJwt, UUID publicKeyId) {
        try {
            String decrypt = jwtEncryption.decrypt(encryptedJwt);

            JwtConsumer jwtConsumer = new JwtConsumerBuilder()
                    .setVerificationKey(securityModuleService.getPublicKeyByKeyId(publicKeyId))
                    .build();

            return jwtConsumer
                    .process(decrypt)
                    .getJwtClaims();
        } catch (Exception e) {
            throw new JwtCreationException("There was an error decrypting JWT");
        }
    }

    public JwtClaims getJwtClaimsFromDecryptedJwt(String encryptedJwt) {
        try {
            String jwt = jwtEncryption.decrypt(encryptedJwt);

            JwtConsumer jwtConsumer = new JwtConsumerBuilder()
                    .setVerificationKey(secretKeyService.getJwtSigningPublicKey())
                    .setSkipAllValidators()
                    .build();

            return jwtConsumer.processToClaims(jwt);
        } catch (Exception e) {
            throw new JwtCreationException("There was an error decrypting JWT");
        }
    }

    public UUID getJwtIdFromDecryptedJwtWithoutValidation(String encryptedJwt) {
        try {
            String jwt = jwtEncryption.decrypt(encryptedJwt);

            JwtConsumer jwtConsumer = new JwtConsumerBuilder()
                    .setDisableRequireSignature()
                    .setSkipSignatureVerification()
                    .build();

            return UUID.fromString(jwtConsumer.processToClaims(jwt).getJwtId());
        } catch (Exception e) {
            throw new JwtCreationException("There was an error decrypting JWT");
        }
    }

    public JwtClaims getJwtClaims(String jwt) {
        try {
            JwtConsumer jwtConsumer = new JwtConsumerBuilder()
                    .setSkipAllValidators()
                    .setDisableRequireSignature()
                    .setSkipSignatureVerification()
                    .build();

            return jwtConsumer.processToClaims(jwt);
        } catch (Exception e) {
            throw new JwtCreationException("There was an error decrypting JWT");
        }
    }


}
