package com.yolt.creditscoring.service.securitymodule.semaevent;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import net.logstash.logback.marker.Markers;
import nl.ing.lovebird.logging.SemaEvent;
import org.slf4j.Marker;
import org.springframework.security.core.GrantedAuthority;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Path to this class is configured in ElastAlert in k8s-manifest-privileged repository.
 * Changing path requires changes in k8s-manifest-privileged.
 * @see <a href="https://git.yolt.io/deployment/k8s-manifests-privileged/-/blob/master/base/elastalert/base/rules/app-ciso-30-cashflow-analyser-admin-login.yml">k8s-manifest-privileged configuration</a>
 */
@Value
@Builder
public class ClientTokenAccessToUnauthorizedEndpointEvent implements SemaEvent {

    @NonNull
    UUID clientId;

    @NonNull
    String endpointURI;

    @NonNull
    List<? extends GrantedAuthority> permissions;

    @Override
    public String getMessage() {
        return "Client Token was used on endpoint without necessary permissions";
    }

    @Override
    public Marker getMarkers() {
        return Markers.append("clientId", clientId)
                .and(Markers.append("endpointURI", endpointURI))
                .and(Markers.append("permissions", permissions.stream()
                        .map(GrantedAuthority::getAuthority).collect(Collectors.joining(","))));
    }
}
