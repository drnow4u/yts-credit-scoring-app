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
 * @see <a href="https://git.yolt.io/deployment/k8s-manifests-privileged/-/blob/master/base/elastalert/base/rules/app-ciso-30-cashflow-analyser-invitation-spikes.yml">k8s-manifest-privileged configuration</a>
 */
@Builder
public class InvitationSpikesSemaEvent implements SemaEvent {

    @NonNull
    private final UUID adminId;
    @NonNull
    private final UUID clientId;

    @Override
    public String getMessage() {
        return "User invited for client ID: " + clientId + " by admin ID: "  + adminId;
    }

    @Override
    public Marker getMarkers() {
        return Markers.append("adminId", adminId)
                .and(Markers.append("clientId", clientId));
    }
}
