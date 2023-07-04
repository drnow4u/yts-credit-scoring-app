package com.yolt.creditscoring.service.userjourney.model;

import com.yolt.creditscoring.service.userjourney.JourneyStatus;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserJourneyRepository extends CrudRepository<UserJourneyMetric, UUID> {

    @Query(value = "SELECT Client.name AS clientname, status, count(DISTINCT user_id) FROM user_journey_metric u " +
            "LEFT JOIN Client ON u.client_id = Client.id " +
            "WHERE created_date BETWEEN ? and ? " +
            "GROUP BY clientname, u.status", nativeQuery = true)
    List<ClientReportRowSet> findByNativeQuery(OffsetDateTime start, OffsetDateTime end);

    @Query(value = "SELECT to_char(created_date, 'YYYY')\\:\\:int as year, to_char(created_date, 'MM')\\:\\:int as month, status, count(DISTINCT user_id) FROM user_journey_metric u " +
            "WHERE client_id = ? AND to_char(created_date, 'YYYY')\\:\\:int = ? " +
            "GROUP BY client_id, status, year, month", nativeQuery = true)
    List<ClientMetricsRowSet> groupUserJourneyMetricsByClientIdAndYear(UUID clientId, int year);

    @Query(value = "SELECT to_char(created_date, 'YYYY')\\:\\:int as year FROM user_journey_metric u " +
            "WHERE client_id = ? " +
            "GROUP BY year", nativeQuery = true)
    List<Integer> findAllAvailableMetricsYears(UUID clientId);

    Optional<UserJourneyMetric> findByClientIdAndUserIdAndStatus(UUID clientId, UUID userId, JourneyStatus status);
}
