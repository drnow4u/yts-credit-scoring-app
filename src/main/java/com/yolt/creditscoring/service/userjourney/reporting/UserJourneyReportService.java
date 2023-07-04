package com.yolt.creditscoring.service.userjourney.reporting;

import com.yolt.creditscoring.configuration.ClockConfig;
import com.yolt.creditscoring.service.userjourney.ClientMetricsDTO;
import com.yolt.creditscoring.service.userjourney.model.ClientMetricsRowSet;
import com.yolt.creditscoring.service.userjourney.model.UserJourneyRepository;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class UserJourneyReportService {

    private static final Clock clock = ClockConfig.getClock();

    private final UserJourneyRepository userJourneyRepository;

    /**
     * The report is calculated for given zoneId.
     * <p>
     * It calculates only completed days e.g. if current date will be 2022-02-20
     * report period will be set to 2022-02-01 00:00:00 - 2022-02-19 23:59:59
     * <p>
     * To prevent different results in summer and winter time offset to calculate report time offset in the reported
     * month is used.
     *
     * @param date   of report
     * @param zoneId of report
     * @return
     */
    public UserJourneyMonthReport reportForMonth(@NonNull LocalDate date, @NonNull ZoneId zoneId) {
        LocalDate previousDay = date.minusDays(1);
        LocalDate reportMonth = previousDay.with(TemporalAdjusters.firstDayOfMonth());

        ZoneOffset zoneOffSet = zoneId.getRules().getOffset(LocalDateTime.of(reportMonth, LocalTime.MIN));

        OffsetDateTime start = OffsetDateTime.of(reportMonth.with(TemporalAdjusters.firstDayOfMonth()), LocalTime.MIN, zoneOffSet);

        LocalDate lastDayOfMonth = reportMonth.with(TemporalAdjusters.lastDayOfMonth());

        OffsetDateTime end = OffsetDateTime.of(lastDayOfMonth, LocalTime.MAX, zoneOffSet);

        List<ClientReport> clientReports = userJourneyRepository.findByNativeQuery(start, end).stream()
                .map(rowSet -> ClientReport.builder()
                        .clientName(rowSet.getClientName())
                        .status(rowSet.getStatus())
                        .count(rowSet.getCount())
                        .build())
                .toList();
        return UserJourneyMonthReport.builder()
                .from(start)
                .till(end)
                .reports(clientReports)
                .build();
    }

    public List<ClientMetricsDTO> getClientMetrics(UUID clientId, Optional<Integer> year) {
        Integer yearForQuery = year.orElseGet(() -> Year.now(clock).getValue());

        List<ClientMetricsRowSet> clientMetricsRowSets = userJourneyRepository.groupUserJourneyMetricsByClientIdAndYear(clientId, yearForQuery);

        return clientMetricsRowSets.stream()
                .map(clientMetricsRowSet ->
                        ClientMetricsDTO.builder()
                                .month(clientMetricsRowSet.getMonth())
                                .year(clientMetricsRowSet.getYear())
                                .count(clientMetricsRowSet.getCount())
                                .status(clientMetricsRowSet.getStatus())
                                .build()
                ).toList();
    }

    public List<Integer> getAllAvailableYearsClientMetricYears(UUID clientId) {
        return userJourneyRepository.findAllAvailableMetricsYears(clientId);
    }
}
