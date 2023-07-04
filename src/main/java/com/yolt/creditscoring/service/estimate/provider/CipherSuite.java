package com.yolt.creditscoring.service.estimate.provider;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class CipherSuite {

    /**
     * Default cipher suites.
     */
    static final List<String> DEFAULT_SUITE = List.of(
            // TLSv1.2
            "TLS_DHE_RSA_WITH_AES_256_GCM_SHA384",
            "TLS_DHE_RSA_WITH_AES_256_CBC_SHA256",
            "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384",
            "TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384",
            "TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384",
            // TLSv1.3
            "TLS_AES_256_GCM_SHA384",
            "TLS_CHACHA20_POLY1305_SHA256",
            "TLS_AES_128_GCM_SHA256"

            // These TLSv1.2 ciphers have been sanctioned by security but are not currently supported:
            // "TLS_ECDHE_RSA_WITH_CHACHA20_POLY1305_SHA256",
            // "TLS_ECDHE_ECDSA_WITH_CHACHA20_POLY1305_SHA256",
    );
}