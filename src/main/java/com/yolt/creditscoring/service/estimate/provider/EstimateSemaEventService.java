package com.yolt.creditscoring.service.estimate.provider;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.logstash.logback.marker.Markers;
import nl.ing.lovebird.logging.SemaEvent;
import nl.ing.lovebird.logging.SemaEventLogger;
import org.slf4j.Marker;
import org.springframework.stereotype.Service;

import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.HashMap;

@Service
public class EstimateSemaEventService {
    private static final Base64.Encoder ENCODER = Base64.getEncoder();

    public void newCertEvent(X509Certificate cert, byte[] digest) {
        final String offeredServerCertBase64 = base64(cert);
        final HashMap<String, String> markers = new HashMap<>();
        markers.put("subject", cert.getSubjectX500Principal().getName());
        markers.put("new-cert", offeredServerCertBase64);
        markers.put("new-cert-sha256", ENCODER.encodeToString(digest));
        SemaEventLogger.log(new ChangedPeerCertificateSEMaEvent("One of our peers new server certificate:\n"
                + "subject: " + cert.getSubjectX500Principal().getName() + "\n"
                + "new cert: " + offeredServerCertBase64 + "\n"
                + "Note: trusting \"new cert\".",
                Markers.appendEntries(markers)
        ));
    }

    public void differentCertEvent(X509Certificate cert, byte[] referenceDigest, byte[] digest) {
        final String offeredServerCertBase64 = base64(cert);
        final HashMap<String, String> markers = new HashMap<>();
        markers.put("subject", cert.getSubjectX500Principal().getName());
        markers.put("old-cert-sha256", ENCODER.encodeToString(referenceDigest));
        markers.put("new-cert", offeredServerCertBase64);
        markers.put("new-cert-sha256", ENCODER.encodeToString(digest));
        SemaEventLogger.log(new ChangedPeerCertificateSEMaEvent("One of our peers rotated their server certificate:\n"
                + "subject: " + cert.getSubjectX500Principal().getName() + "\n"
                + "reference cert SHA-256: " + ENCODER.encodeToString(referenceDigest) + "\n"
                + "new cert: " + offeredServerCertBase64 + "\n"
                + "Note: not trusting \"new cert\".",
                Markers.appendEntries(markers)
        ));
    }

    /**
     * This method is used during construction of the SEMa event.  We want the SEMa event to be logged
     * at all costs which is why we swallow an exception that might occur during base64 encoding of the
     * certificate.
     */
    private static String base64(final X509Certificate certificate) {
        try {
            return ENCODER.encodeToString(certificate.getEncoded());
        } catch (Exception e) {
            return "<failed to encode>";
        }
    }

    @Getter(onMethod_ = @Override)
    @RequiredArgsConstructor
    static class ChangedPeerCertificateSEMaEvent implements SemaEvent {
        private final String message;
        private final Marker markers;
    }
}
