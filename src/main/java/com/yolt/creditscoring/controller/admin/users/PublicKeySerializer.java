package com.yolt.creditscoring.controller.admin.users;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import org.springframework.boot.jackson.JsonComponent;

import java.io.IOException;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;

@JsonComponent
public class PublicKeySerializer extends JsonSerializer<PublicKey> {

    public static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();

    @Override
    public void serialize(PublicKey publicKey, JsonGenerator jsonGenerator,
                          SerializerProvider serializerProvider) throws IOException {
        RSAKey jwk = new RSAKey.Builder((RSAPublicKey) publicKey)
                .keyUse(KeyUse.SIGNATURE)
                .keyID("")
                .build();

        jsonGenerator.writeString(ENCODER.encodeToString(jwk.getModulus().decode()));
    }

}
