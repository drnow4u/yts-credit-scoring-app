package com.yolt.creditscoring.service.securitymodule.semaevent;

import lombok.Builder;
import lombok.NonNull;
import net.logstash.logback.marker.Markers;
import nl.ing.lovebird.logging.SemaEvent;
import org.slf4j.Marker;

/**
 * Path to this class is configured in ElastAlert in k8s-manifest-privileged repository.
 * Changing path requires changes in k8s-manifest-privileged.
 * @see <a href="https://git.yolt.io/deployment/k8s-manifests-privileged/-/blob/master/base/elastalert/base/rules/app-ciso-30-cashflow-analyser-admin-login.yml">k8s-manifest-privileged configuration</a>
 */
@Builder
public class NotRegisteredAdminLoginSemaEvent implements SemaEvent {

    @NonNull
    private String idpId;

    @NonNull
    private String provider;

    @NonNull
    private String ipAddress;

    @Override
    public String getMessage() {
        return "Not registered admin tried to access Cashlflow Analyser application";
    }

    @Override
    public Marker getMarkers() {
        return Markers.append("idpId", idpId)
                .and(Markers.append("provider", provider))
                .and(Markers.append("ipAddress", ipAddress));
    }
}
