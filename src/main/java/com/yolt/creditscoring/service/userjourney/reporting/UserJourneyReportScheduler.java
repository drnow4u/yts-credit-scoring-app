package com.yolt.creditscoring.service.userjourney.reporting;

import com.yolt.creditscoring.configuration.ClockConfig;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;

@Slf4j
@Service
@AllArgsConstructor
public class UserJourneyReportScheduler {

    private static final Clock amsterdamClock = ClockConfig.getAmsterdamClock();
    private final UserJourneyReportService userJourneyReportService;

    @Scheduled(cron = "${credit-scoring.invoicing.report-cron:0 0 0 * * *}")
    public void scheduleFixedDelayTask() {
        LocalDate now = LocalDate.now(amsterdamClock);

        UserJourneyMonthReport clientReports = userJourneyReportService.reportForMonth(now, amsterdamClock.getZone());

        for (ClientReport report: clientReports.getReports()) {
            MDC.put("invoicing-client", report.getClientName());
            MDC.put("invoicing-from", clientReports.getFrom().toString());
            MDC.put("invoicing-till", clientReports.getTill().toString());
            MDC.put("invoicing-users", String.valueOf(report.getCount()));
            MDC.put("invoicing-status", report.getStatus().name());
            MDC.put("invoicing-year", String.valueOf(now.getYear()));
            MDC.put("invoicing-month", String.valueOf(now.getMonthValue()));
            log.info("Invoice {}-{}: Report for client {} with status {} has distinct {} user(s) (so far).",
                    now.getYear(), now.getMonthValue(), report.getClientName(), report.getStatus(), report.getCount()); //NOSHERIFF Used invoicing report in Kibana
            MDC.remove("invoicing-client");
            MDC.remove("invoicing-from");
            MDC.remove("invoicing-till");
            MDC.remove("invoicing-users");
            MDC.remove("invoicing-status");
            MDC.remove("invoicing-year");
            MDC.remove("invoicing-month");
        }
    }
}
