package com.yolt.creditscoring.configuration.security.admin;

import nl.ing.lovebird.http.YoltProxySelector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.ProxySelector;

/**
 * This factory helps choose the correct {@link ProxySelector}.
 * <p>
 * Different environments may need a different proxy.
 * However, locally you may not want to use a proxy at all.
 * <p>
 * If `isp.proxy.host/port` have not been configured,
 * the default ProxySelector will be created.
 */
@Component
public class ProxySelectorFactory {

    private final String ispProxyHost;
    private final Integer ispProxyPort;

    public ProxySelectorFactory(
            @Value("${isp.proxy.host:#{null}}") String ispProxyHost,
            @Value("${isp.proxy.port:#{null}}") Integer ispProxyPort
    ) {
        this.ispProxyHost = ispProxyHost;
        this.ispProxyPort = ispProxyPort;
    }

    public ProxySelector create() {
        if (ispProxyHost == null || ispProxyPort == null) {
            return ProxySelector.getDefault();
        }
        return new YoltProxySelector(ispProxyHost, ispProxyPort);
    }

}
