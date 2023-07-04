package com.yolt.creditscoring.service.securitymodule.semaevent;

import lombok.Builder;
import net.logstash.logback.marker.Markers;
import nl.ing.lovebird.logging.SemaEvent;
import org.slf4j.Marker;

import java.util.UUID;

/**
 * Path to this class is configured in ElastAlert in k8s-manifest-privileged repository.
 * Changing path requires changes in k8s-manifest-privileged.
 * @see <a href="https://git.yolt.io/deployment/k8s-manifests-privileged/-/blob/master/base/elastalert/base/rules/app-ciso-30-cashflow-analyser-invalid-signature.yml">k8s-manifest-privileged configuration</a>
 */
@Builder
public class InvalidSignatureSemaEvent implements SemaEvent {

    private final String message;
    private final UUID alarmTriggeredBy;
    private final String signature;
    private final UUID userId;
    private final UUID clientId;

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public Marker getMarkers() {
        return Markers.append("alarmTriggeredBy", alarmTriggeredBy)
                .and(Markers.append("signature", signature))
                .and(Markers.append("clientId", clientId))
                .and(Markers.append("userId", userId));
    }
}
