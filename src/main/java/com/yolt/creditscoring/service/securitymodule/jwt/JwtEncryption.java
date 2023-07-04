package com.yolt.creditscoring.service.securitymodule.jwt;

import com.yolt.creditscoring.exception.EncryptionException;
import com.yolt.creditscoring.service.securitymodule.vault.VaultSecretKeyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import java.util.Base64;

@RequiredArgsConstructor
@Service
public class JwtEncryption {

    private static final String ENCODING_ALGORITHM = "AES/ECB/PKCS5Padding";
    private final VaultSecretKeyService secretKeyService;

    public String encrypt(String jwtForEncryption) {
        try {
            Cipher cipher = Cipher.getInstance(ENCODING_ALGORITHM); //NOSONAR - we do not story vulnerable data in JWT, so ECB encryption should be enough. The JWT is sign with a separate key.
            cipher.init(Cipher.ENCRYPT_MODE, secretKeyService.getJwtEncryptionKey());
            return Base64.getUrlEncoder().withoutPadding().encodeToString(cipher.doFinal(jwtForEncryption.getBytes("UTF-8")));
        } catch (Exception e) {
            throw new EncryptionException("There was an error when encrypting JWT");
        }
    }

    public String decrypt(String jwtToDecrypt) {
        try {
            Cipher cipher = Cipher.getInstance(ENCODING_ALGORITHM); //NOSONAR
            cipher.init(Cipher.DECRYPT_MODE, secretKeyService.getJwtEncryptionKey());
            return new String(cipher.doFinal(Base64.getUrlDecoder().decode(jwtToDecrypt)));
        } catch (Exception e) {
            throw new EncryptionException("There was an error when decrypting JWT");
        }
    }
}
