package com.yolt.creditscoring.service.securitymodule.semaevent;

import lombok.Builder;
import lombok.NonNull;
import net.logstash.logback.marker.Markers;
import nl.ing.lovebird.logging.SemaEvent;
import org.slf4j.Marker;
import org.springframework.lang.Nullable;

import java.util.UUID;

/**
 * Path to this class is configured in ElastAlert in k8s-manifest-privileged repository.
 * Changing path requires changes in k8s-manifest-privileged.
 * @see <a href="https://git.yolt.io/deployment/k8s-manifests-privileged/-/blob/master/base/elastalert/base/rules/app-ciso-30-cashflow-analyser-admin-login.yml">k8s-manifest-privileged configuration</a>
 */
@Builder
public class AdminLoginSemaEvent implements SemaEvent {

    @NonNull
    private final String idpId;
    @Nullable
    private final UUID clientId;
    @Nullable
    private final String adminEmail;

    @Override
    public String getMessage() {
        return "Admin with following idpId has logged into application: " + idpId;
    }

    @Override
    public Marker getMarkers() {
        return Markers.append("idpId", idpId)
                .and(Markers.append("clientId", clientId))
                .and(Markers.append("adminEmail", adminEmail));
    }
}
