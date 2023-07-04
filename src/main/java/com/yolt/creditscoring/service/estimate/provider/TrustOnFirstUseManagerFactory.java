package com.yolt.creditscoring.service.estimate.provider;

import io.netty.handler.ssl.util.SimpleTrustManagerFactory;

import javax.net.ssl.ManagerFactoryParameters;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class TrustOnFirstUseManagerFactory extends SimpleTrustManagerFactory {
    private final TrustManagerFactory trustManagerFactory;
    private final EstimateSemaEventService estimateSemaEventService;

    public TrustOnFirstUseManagerFactory(TrustManagerFactory trustManagerFactory, EstimateSemaEventService estimateSemaEventService) {
        super();
        this.trustManagerFactory = trustManagerFactory;
        this.estimateSemaEventService = estimateSemaEventService;
    }

    @Override
    protected void engineInit(KeyStore keyStore) throws Exception {
        // This ManagerFactory is not using KeyStore
    }

    @Override
    protected void engineInit(ManagerFactoryParameters managerFactoryParameters) throws Exception {
        // This ManagerFactory is not using any parameters
    }

    @Override
    protected TrustManager[] engineGetTrustManagers() {
        return new TrustManager[]{new TrustOnFirstUseManager(trustManagerFactory.getTrustManagers(), estimateSemaEventService)};
    }

    static class TrustOnFirstUseManager implements X509TrustManager {
        private final TrustManager[] trustManagers;
        private final Map<String, byte[]> fingerprints = new ConcurrentHashMap<>();
        private final EstimateSemaEventService estimateSemaEventService;

        public TrustOnFirstUseManager(TrustManager[] trustManagers, EstimateSemaEventService estimateSemaEventService) {
            this.trustManagers = trustManagers;
            this.estimateSemaEventService = estimateSemaEventService;
        }

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            for (TrustManager tm : trustManagers) {
                if (tm instanceof X509TrustManager x509tm) {
                    x509tm.checkClientTrusted(chain, authType);
                }
            }
            checkTrusted(chain);
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            for (TrustManager tm : trustManagers) {
                if (tm instanceof X509TrustManager x509tm) {
                    x509tm.checkServerTrusted(chain, authType);
                }
            }
            checkTrusted(chain);
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            var issuers = new ArrayList<X509Certificate>();
            for (TrustManager tm : trustManagers) {
                if (tm instanceof X509TrustManager x509tm) {
                    issuers.addAll(List.of(x509tm.getAcceptedIssuers()));
                }
            }
            return issuers.toArray(new X509Certificate[0]);
        }

        private void checkTrusted(X509Certificate[] chain) throws CertificateException {
            try {
                X509Certificate cert = chain[0];
                var md = MessageDigest.getInstance("SHA-256");
                var currentDigest = md.digest(cert.getEncoded());

                var referenceDigest = fingerprints.get(cert.getSubjectX500Principal().getName());
                if (referenceDigest == null) {
                    fingerprints.computeIfAbsent(cert.getSubjectX500Principal().getName(), subjectName -> {
                        estimateSemaEventService.newCertEvent(cert, currentDigest);
                        return currentDigest;
                    });
                } else if (!Arrays.equals(referenceDigest, currentDigest)) {
                    estimateSemaEventService.differentCertEvent(cert, referenceDigest, currentDigest);
                    throw new CertificateException("Server certificate with unknown fingerprint: " + cert.getSubjectX500Principal().getName());
                }
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalArgumentException("Unsupported hash algorithm: SHA-256");
            }

        }

    }

}
