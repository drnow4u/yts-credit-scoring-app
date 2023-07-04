package com.yolt.creditscoring.service.securitymodule.jwt;

import com.yolt.creditscoring.exception.EncryptionException;
import com.yolt.creditscoring.exception.JwtCreationException;
import com.yolt.creditscoring.service.securitymodule.vault.VaultSecretKeyService;
import org.assertj.core.api.Assertions;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.NumericDate;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.jwt.consumer.JwtContext;
import org.jose4j.lang.JoseException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationResponseType;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.*;

import static com.yolt.creditscoring.TestUtils.SOME_USER_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class JwtCreationServiceTest {

    @Mock
    private VaultSecretKeyService secretKeyService;

    @Mock
    private JwtEncryption jwtEncryption;

    @InjectMocks
    private JwtCreationService jwtCreationService;

    @Test
    void shouldCreateEncodedJWT() throws Exception {
        // Given
        ArgumentCaptor<String> jwsCaptor = ArgumentCaptor.forClass(String.class);
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        KeyPair keyPair = keyGen.generateKeyPair();
        PrivateKey privateKey = keyPair.getPrivate();
        PublicKey publicKey = keyPair.getPublic();

        given(secretKeyService.getJwtSigningPrivateKey()).willReturn(privateKey);

        // When
        jwtCreationService.createUserToken(SOME_USER_ID.toString());

        // Then
        then(jwtEncryption).should().encrypt(jwsCaptor.capture());
        String jwtToken = jwsCaptor.getValue();

        jwtToken = jwtToken.replace("Bearer ", "");

        JwtConsumer jwtConsumer = new JwtConsumerBuilder()
                .setVerificationKey(publicKey)
                .build();

        JwtContext jwtContext = jwtConsumer.process(jwtToken);

        Assertions.assertThat(jwtContext.getJwtClaims().getSubject()).isEqualTo(SOME_USER_ID.toString());
    }

    @Test
    void shouldThrowJwtCreationExceptionWhenErrorOccurDuringJwtEncryption() throws Exception {
        // Given
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        KeyPair keyPair = keyGen.generateKeyPair();
        PrivateKey privateKey = keyPair.getPrivate();

        given(secretKeyService.getJwtSigningPrivateKey()).willReturn(privateKey);
        given(jwtEncryption.encrypt(anyString())).willThrow(new EncryptionException("exception"));

        // When
        Throwable thrown = catchThrowable(() -> jwtCreationService.createUserToken(SOME_USER_ID.toString()));

        // Then
        assertThat(thrown).isInstanceOf(JwtCreationException.class);
    }

    @Test
    void shouldCreateEncodedJWTForCookie() throws Exception {
        // Given
        ArgumentCaptor<String> jwsCaptor = ArgumentCaptor.forClass(String.class);
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        KeyPair keyPair = keyGen.generateKeyPair();
        PrivateKey privateKey = keyPair.getPrivate();
        PublicKey publicKey = keyPair.getPublic();

        given(secretKeyService.getJwtSigningPrivateKey()).willReturn(privateKey);

        Map<String, Object> expectedMapAttribute = new LinkedHashMap<>();
        expectedMapAttribute.put("key", "value");
        OAuth2AuthorizationRequest request = OAuth2AuthorizationRequest.authorizationCode()
                .authorizationUri("SOME_AUTHORIZATION_URI")
                .clientId("SOME_CLIENT_ID")
                .redirectUri("SOME_REDIRECT_URI")
                .authorizationRequestUri("SOME_AUTHORIZATION_REQUEST_URI")
                .scopes(new HashSet<>(new ArrayList<>(List.of("SOME_SCOPE"))))
                .state("SOME_STATE")
                .additionalParameters(expectedMapAttribute)
                .attributes(expectedMapAttribute)
                .build();

        // When
        jwtCreationService.createJWTCookie(request);

        // Then
        then(jwtEncryption).should().encrypt(jwsCaptor.capture());
        String jwtToken = jwsCaptor.getValue();

        JwtConsumer jwtConsumer = new JwtConsumerBuilder()
                .setVerificationKey(publicKey)
                .build();

        JwtClaims jwtClaims = jwtConsumer.process(jwtToken).getJwtClaims();

        Assertions.assertThat(jwtClaims.getSubject()).isEqualTo("Auth Cookie");
        Assertions.assertThat(jwtClaims.getClaimValue("authorizationUri")).isEqualTo("SOME_AUTHORIZATION_URI");
        Assertions.assertThat(jwtClaims.getClaimValue("authorizationGrantType")).isEqualTo(AuthorizationGrantType.AUTHORIZATION_CODE.getValue());
        Assertions.assertThat(jwtClaims.getClaimValue("responseType")).isEqualTo(OAuth2AuthorizationResponseType.CODE.getValue());
        Assertions.assertThat(jwtClaims.getClaimValue("clientId")).isEqualTo( "SOME_CLIENT_ID");
        Assertions.assertThat(jwtClaims.getClaimValue("redirectUri")).isEqualTo("SOME_REDIRECT_URI");
        Assertions.assertThat(jwtClaims.getClaimValue("scopes")).isEqualTo(new ArrayList<>(List.of("SOME_SCOPE")));
        Assertions.assertThat(jwtClaims.getClaimValue("state")).isEqualTo("SOME_STATE");
        Assertions.assertThat(jwtClaims.getClaimValue("additionalParameters")).isEqualTo(expectedMapAttribute);
        Assertions.assertThat(jwtClaims.getClaimValue("authorizationRequestUri")).isEqualTo("SOME_AUTHORIZATION_REQUEST_URI");
        Assertions.assertThat(jwtClaims.getClaimValue("attributes")).isEqualTo(expectedMapAttribute);
    }

    @Test
    void shouldGetJwtClaimsFromDecryptedJwtAndPerformValidation() throws Exception {
        // Given
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        KeyPair keyPair = keyGen.generateKeyPair();
        PrivateKey privateKey = keyPair.getPrivate();
        PublicKey publicKey = keyPair.getPublic();

        given(jwtEncryption.decrypt("SOME_ENCODED_JWT")).willReturn(createTestJwtSignWithGivenPrivateKey(privateKey, false));
        given(secretKeyService.getJwtSigningPublicKey()).willReturn(publicKey);

        // When
        JwtClaims result = jwtCreationService.getJwtClaimsFromDecryptedJwtAndPerformValidation("SOME_ENCODED_JWT");

        // Then
        Assertions.assertThat(result.getSubject()).isEqualTo(SOME_USER_ID.toString());
    }

    @Test
    void shouldThrownAnErrorWhenJWTCannotBeFetchDueValidationError() throws Exception {
        // Given
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        KeyPair keyPair = keyGen.generateKeyPair();
        KeyPair differentKeyPair = keyGen.generateKeyPair();
        PrivateKey privateKey = keyPair.getPrivate();
        PublicKey publicKey = differentKeyPair.getPublic();

        given(jwtEncryption.decrypt("SOME_ENCODED_JWT")).willReturn(createTestJwtSignWithGivenPrivateKey(privateKey, false));
        given(secretKeyService.getJwtSigningPublicKey()).willReturn(publicKey);

        // When
        Throwable thrown = catchThrowable(() ->jwtCreationService
                .getJwtClaimsFromDecryptedJwtAndPerformValidation("SOME_ENCODED_JWT"));

        // Then
        assertThat(thrown).isInstanceOf(JwtCreationException.class)
                .hasMessageContaining("There was an error decrypting JWT");
    }


    @Test
    void shouldThrownAnErrorWhenJWTCannotBeFetchDueDecryptError() throws Exception {
        // Given
        given(jwtEncryption.decrypt("SOME_ENCODED_JWT")).willThrow(new EncryptionException("exception"));

        // When
        Throwable thrown = catchThrowable(() ->jwtCreationService
                .getJwtClaimsFromDecryptedJwtAndPerformValidation("SOME_ENCODED_JWT"));

        // Then
        assertThat(thrown).isInstanceOf(JwtCreationException.class)
                .hasMessageContaining("There was an error decrypting JWT");
    }

    @Test
    void shouldGetJwtClaimsFromDecryptedJwt() throws Exception {
        // Given
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        KeyPair keyPair = keyGen.generateKeyPair();
        PrivateKey privateKey = keyPair.getPrivate();
        PublicKey publicKey = keyPair.getPublic();

        given(jwtEncryption.decrypt("SOME_ENCODED_JWT")).willReturn(createTestJwtSignWithGivenPrivateKey(privateKey, true));
        given(secretKeyService.getJwtSigningPublicKey()).willReturn(publicKey);

        // When
        JwtClaims result = jwtCreationService.getJwtClaimsFromDecryptedJwt("SOME_ENCODED_JWT");

        // Then
        Assertions.assertThat(result.getSubject()).isEqualTo(SOME_USER_ID.toString());
    }

    private String createTestJwtSignWithGivenPrivateKey(PrivateKey privateKey, boolean expiredJwt) throws JoseException {
        JwtClaims claims = new JwtClaims();
        claims.setSubject(SOME_USER_ID.toString());
        claims.setIssuedAtToNow();
        claims.setJwtId(UUID.randomUUID().toString());

        if(expiredJwt) {
            claims.setExpirationTime(NumericDate.fromMilliseconds(System.currentTimeMillis() - 1000L));
        } else {
            claims.setExpirationTimeMinutesInTheFuture(60);
        }

        JsonWebSignature jws = new JsonWebSignature();
        jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.RSA_USING_SHA512);
        jws.setPayload(claims.toJson());

        jws.setKey(privateKey);

        return jws.getCompactSerialization();
    }
}
