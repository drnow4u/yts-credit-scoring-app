package com.yolt.creditscoring.service.userjourney;

import com.yolt.creditscoring.configuration.ClockConfig;
import com.yolt.creditscoring.service.client.model.ClientEntity;
import com.yolt.creditscoring.service.client.model.ClientRepository;
import com.yolt.creditscoring.service.userjourney.model.UserJourneyMetric;
import com.yolt.creditscoring.service.userjourney.model.UserJourneyRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserJourneyService {

    private static final Clock clock = ClockConfig.getClock();

    private final UserJourneyRepository userJourneyRepository;
    private final MeterRegistry registry;
    private final ClientRepository clientRepository;
    private final Map<String, Counter> counters = new HashMap<>();

    public void registerInvited(@NonNull UUID clientId, @NotNull UUID userId) {
        UserJourneyMetric userJourneyMetric = createUserJourneyMetric(clientId, userId);
        userJourneyMetric.setStatus(JourneyStatus.INVITED);

        userJourneyRepository.save(userJourneyMetric);

        metricIncrement(JourneyStatus.INVITED, clientId);
    }

    public void registerConsentGenerated(@NonNull UUID clientId, @NotNull UUID userId) {
        UserJourneyMetric userJourneyMetric = createUserJourneyMetric(clientId, userId);
        userJourneyMetric.setStatus(JourneyStatus.CONSENT_ACCEPTED);

        userJourneyRepository.save(userJourneyMetric);

        metricIncrement(JourneyStatus.CONSENT_ACCEPTED, clientId);
    }

    public void registerReportGenerated(@NonNull UUID clientId, @NotNull UUID userId) {
        UserJourneyMetric userJourneyMetric = createUserJourneyMetric(clientId, userId);
        userJourneyMetric.setStatus(JourneyStatus.REPORT_GENERATED);

        userJourneyRepository.save(userJourneyMetric);

        metricIncrement(JourneyStatus.REPORT_GENERATED, clientId);
    }

    public void registerReportSaved(@NonNull UUID clientId, @NotNull UUID userId) {
        UserJourneyMetric userJourneyMetric = createUserJourneyMetric(clientId, userId);
        userJourneyMetric.setStatus(JourneyStatus.REPORT_SAVED);

        userJourneyRepository.save(userJourneyMetric);

        metricIncrement(JourneyStatus.REPORT_SAVED, clientId);
    }

    public void registerReportRefused(@NonNull UUID clientId, @NotNull UUID userId) {
        UserJourneyMetric userJourneyMetric = createUserJourneyMetric(clientId, userId);
        userJourneyMetric.setStatus(JourneyStatus.REPORT_REFUSED);

        userJourneyRepository.save(userJourneyMetric);

        metricIncrement(JourneyStatus.REPORT_REFUSED, clientId);
    }

    public void registerConsentRefuse(@NonNull UUID clientId, @NotNull UUID userId) {
        UserJourneyMetric userJourneyMetric = createUserJourneyMetric(clientId, userId);
        userJourneyMetric.setStatus(JourneyStatus.CONSENT_REFUSED);

        userJourneyRepository.save(userJourneyMetric);

        metricIncrement(JourneyStatus.CONSENT_REFUSED, clientId);
    }

    public void registerBankConsentAccept(@NonNull UUID clientId, @NotNull UUID userId) {
        UserJourneyMetric userJourneyMetric = createUserJourneyMetric(clientId, userId);
        userJourneyMetric.setStatus(JourneyStatus.BANK_CONSENT_ACCEPTED);

        userJourneyRepository.save(userJourneyMetric);

        metricIncrement(JourneyStatus.BANK_CONSENT_ACCEPTED, clientId);
    }

    public void registerBankConsentRefused(@NonNull UUID clientId, @NotNull UUID userId) {
        UserJourneyMetric userJourneyMetric = createUserJourneyMetric(clientId, userId);
        userJourneyMetric.setStatus(JourneyStatus.BANK_CONSENT_REFUSED);

        userJourneyRepository.save(userJourneyMetric);

        metricIncrement(JourneyStatus.BANK_CONSENT_REFUSED, clientId);
    }

    public void registerBankError(@NonNull UUID clientId, @NotNull UUID userId) {
        UserJourneyMetric userJourneyMetric = createUserJourneyMetric(clientId, userId);
        userJourneyMetric.setStatus(JourneyStatus.BANK_ERROR);

        userJourneyRepository.save(userJourneyMetric);

        metricIncrement(JourneyStatus.BANK_ERROR, clientId);
    }

    public void registerExpired(@NonNull UUID clientId, @NotNull UUID userId) {
        UserJourneyMetric userJourneyMetric = createUserJourneyMetric(clientId, userId);
        userJourneyMetric.setStatus(JourneyStatus.EXPIRED);

        userJourneyRepository.save(userJourneyMetric);

        metricIncrement(JourneyStatus.EXPIRED, clientId);
    }

    private static UserJourneyMetric createUserJourneyMetric(@NonNull UUID clientId, @NotNull UUID userId) {
        UserJourneyMetric userJourneyMetric = new UserJourneyMetric();
        userJourneyMetric.setId(UUID.randomUUID());
        userJourneyMetric.setClientId(clientId);
        userJourneyMetric.setUserId(userId);
        userJourneyMetric.setCreatedDate(OffsetDateTime.now(clock));
        return userJourneyMetric;
    }

    private void metricIncrement(JourneyStatus journeyStatus, UUID clientId) {
        Counter counter = counters.computeIfAbsent(journeyStatus.name() + "_" + clientId.toString(), s -> {
            ClientEntity client = clientRepository.findById(clientId).orElseThrow();
            return Counter.builder("users_journey_" + journeyStatus.name().toLowerCase())
                    .tags("invoicing_client", client.getName())
                    .register(registry);
        });

        counter.increment();
    }

    public boolean isConsentGeneratedRegistered(UUID clientId, UUID userId) {
        return userJourneyRepository.findByClientIdAndUserIdAndStatus(clientId, userId, JourneyStatus.CONSENT_ACCEPTED)
                .isPresent();
    }
}
