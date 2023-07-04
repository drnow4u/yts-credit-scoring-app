package com.yolt.creditscoring.service.securitymodule.semaevent;

import lombok.Builder;
import lombok.NonNull;
import net.logstash.logback.marker.Markers;
import nl.ing.lovebird.logging.SemaEvent;
import org.slf4j.Marker;

import java.util.UUID;

/**
 * Path to this class is configured in ElastAlert in k8s-manifest-privileged repository.
 * Changing path requires changes in k8s-manifest-privileged.
 * @see <a href="https://git.yolt.io/deployment/k8s-manifests-privileged/-/blob/master/base/elastalert/base/rules/app-ciso-30-cashflow-analyser-admin-login.yml">k8s-manifest-privileged configuration</a>
 */
@Builder
public class AdminEmailDoesNotMatchIdpIdEvent implements SemaEvent {

    @NonNull
    private final UUID clientId;

    @NonNull
    private final String idpId;

    @NonNull
    private final String storedEmail;

    @NonNull
    private final String responseEmail;

    @NonNull
    private final String provider;

    @Override
    public String getMessage() {
        return "Admin with following idp ID: " + idpId + " has not match email";
    }

    @Override
    public Marker getMarkers() {
        return Markers.append("clientId", clientId)
                .and(Markers.append("idpId", idpId))
                .and(Markers.append("storedEmail", storedEmail))
                .and(Markers.append("responseEmail", responseEmail))
                .and(Markers.append("provider", provider));
    }
}
