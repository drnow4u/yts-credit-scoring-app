package com.yolt.creditscoring.controller.user.creditscore;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.function.BooleanSupplier;

import static com.yolt.creditscoring.configuration.AsyncConfiguration.CREDIT_SCORE_EXECUTOR;

@Slf4j
@Service
public class ReportCalculatorRunnerService {

    private final int retryAmount;
    private final int initialRetryInterval;

    public ReportCalculatorRunnerService(int retryAmount, int initialRetryInterval) {
        this.retryAmount = retryAmount;
        this.initialRetryInterval = initialRetryInterval;
    }

    public ReportCalculatorRunnerService() {
        this(3, 20 * 1000);
    }

    @Async(CREDIT_SCORE_EXECUTOR)
    public void executeAsyncReportCalculation(final BooleanSupplier booleanSupplier) {
        int retryIntervalMillis = initialRetryInterval;

        try {
            for (int i = 0; i < retryAmount; i++) {
                if (booleanSupplier.getAsBoolean()) {
                    return;
                } else {
                    Thread.sleep(retryIntervalMillis);
                    retryIntervalMillis *= 2;
                    log.warn("Yolt API not ready: {}", i);
                }
            }

            log.error("Report not calculated after retry: {}", retryAmount);
        } catch (InterruptedException e) {
            log.warn("Report calculation error");
        }
    }

}
