package com.yolt.creditscoring.service.estimate.provider;

import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.net.ssl.X509TrustManager;
import javax.security.auth.x500.X500Principal;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrustOnFirstUseManagerFactoryTest {

    @Test
    void shouldTrustServerOnFirstUse() throws CertificateException {
        // Given
        var semaEventService = mock(EstimateSemaEventService.class);

        var cert = mock(X509Certificate.class);
        given(cert.getSubjectX500Principal()).willReturn(new X500Principal("CN=Duke, OU=JavaSoft, O=Sun Microsystems, C=US"));
        given(cert.getEncoded()).willReturn("Some cert bytes".getBytes());

        var tmf = new TrustOnFirstUseManagerFactory(InsecureTrustManagerFactory.INSTANCE, semaEventService);
        var tms = tmf.getTrustManagers();

        // When
        ((X509TrustManager) tms[0]).checkServerTrusted(new X509Certificate[]{cert}, "");

        // Then
        then(semaEventService).should(times(1)).newCertEvent(eq(cert), any());
        then(semaEventService).should(never()).differentCertEvent(any(), any(), any());

    }

    @Test
    void shouldTrustServerOnNextUse() throws CertificateException {
        // Given
        var semaEventService = mock(EstimateSemaEventService.class);

        var cert = mock(X509Certificate.class);
        given(cert.getSubjectX500Principal()).willReturn(new X500Principal("CN=Duke, OU=JavaSoft, O=Sun Microsystems, C=US"));
        given(cert.getEncoded()).willReturn("Some cert bytes".getBytes());

        var tmf = new TrustOnFirstUseManagerFactory(InsecureTrustManagerFactory.INSTANCE, semaEventService);
        var tms = tmf.getTrustManagers();

        // The first use
        ((X509TrustManager) tms[0]).checkServerTrusted(new X509Certificate[]{cert}, "");

        // When
        ((X509TrustManager) tms[0]).checkServerTrusted(new X509Certificate[]{cert}, "");

        // Then
        then(semaEventService).should(times(1)).newCertEvent(eq(cert), any());
        then(semaEventService).should(never()).differentCertEvent(any(), any(), any());

    }

    @Test
    void shouldNotTrustServerWhenRotate() throws CertificateException {
        // Given
        var semaEventService = mock(EstimateSemaEventService.class);

        var cert = mock(X509Certificate.class);
        given(cert.getSubjectX500Principal()).willReturn(new X500Principal("CN=Duke, OU=JavaSoft, O=Sun Microsystems, C=US"));
        given(cert.getEncoded()).willReturn("Some cert bytes".getBytes());

        var rotatedCert = mock(X509Certificate.class);
        given(rotatedCert.getSubjectX500Principal()).willReturn(new X500Principal("CN=Duke, OU=JavaSoft, O=Sun Microsystems, C=US"));
        given(rotatedCert.getEncoded()).willReturn("Rotated cert bytes".getBytes());

        var tmf = new TrustOnFirstUseManagerFactory(InsecureTrustManagerFactory.INSTANCE, semaEventService);
        var tms = tmf.getTrustManagers();

        // The first use
        ((X509TrustManager) tms[0]).checkServerTrusted(new X509Certificate[]{cert}, "");

        // When
        Executable executable = () -> {
            ((X509TrustManager) tms[0]).checkServerTrusted(new X509Certificate[]{rotatedCert}, "");
        };

        // Then
        assertThrows(CertificateException.class, executable);
        then(semaEventService).should(times(1)).newCertEvent(eq(cert), any());
        then(semaEventService).should(times(1)).differentCertEvent(eq(rotatedCert), any(), any());
    }

}
