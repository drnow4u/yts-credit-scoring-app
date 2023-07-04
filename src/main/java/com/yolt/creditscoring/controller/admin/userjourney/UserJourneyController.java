package com.yolt.creditscoring.controller.admin.userjourney;

import com.yolt.creditscoring.configuration.security.SecurityRoles;
import com.yolt.creditscoring.configuration.security.admin.ClientAdminAuthenticationPrincipal;
import com.yolt.creditscoring.configuration.security.admin.ClientAdminPrincipal;
import com.yolt.creditscoring.service.userjourney.ClientMetricsDTO;
import com.yolt.creditscoring.service.userjourney.reporting.UserJourneyReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@Secured(value = SecurityRoles.ROLE_PREFIX + SecurityRoles.CLIENT_ADMIN)
public class UserJourneyController {

    public static final String ADMIN_METRICS_ENDPOINT = "/api/admin/metrics";
    public static final String ADMIN_METRICS_YEARS_ENDPOINT = "/api/admin/metrics/years";
    public final UserJourneyReportService userJourneyReportService;

    @GetMapping(ADMIN_METRICS_ENDPOINT)
    public List<ClientMetricsDTO> getClientUserMetrics(@ClientAdminAuthenticationPrincipal ClientAdminPrincipal principal,
                                                       @RequestParam Optional<Integer> year) {
        return userJourneyReportService.getClientMetrics(principal.getClientId(), year);
    }

    @GetMapping(ADMIN_METRICS_YEARS_ENDPOINT)
    public List<Integer> getAllAvailableYears(@ClientAdminAuthenticationPrincipal ClientAdminPrincipal principal) {
        return userJourneyReportService.getAllAvailableYearsClientMetricYears(principal.getClientId());
    }
}
