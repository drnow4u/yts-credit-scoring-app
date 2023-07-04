package com.yolt.creditscoring.controller.user.creditscore;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.BDDAssertions.then;

class ReportCalculatorRunnerServiceTest {

    @Test
    void shouldAsyncReportCalculationExecuteOneTime() {
        // Given
        var runnerService = new ReportCalculatorRunnerService(3, 20);

        final AtomicInteger runCounter = new AtomicInteger();

        // When
        runnerService.executeAsyncReportCalculation(() -> runCounter.incrementAndGet() == 1);

        // Then
        then(runCounter.get()).isEqualTo(1);
    }

    @Test
    void shouldAsyncReportCalculationExecuteTwoTimes() {
        // Given
        var runnerService = new ReportCalculatorRunnerService(3, 20);

        final AtomicInteger runCounter = new AtomicInteger();

        // When
        runnerService.executeAsyncReportCalculation(() -> runCounter.incrementAndGet() == 2);

        // Then
        then(runCounter.get()).isEqualTo(2);
    }

    @Test
    void shouldAsyncReportCalculationExecuteThreeTimes() {
        // Given
        var runnerService = new ReportCalculatorRunnerService(3, 20);

        final AtomicInteger runCounter = new AtomicInteger();

        // When
        runnerService.executeAsyncReportCalculation(() -> runCounter.incrementAndGet() == 3);

        // Then
        then(runCounter.get()).isEqualTo(3);
    }

    @Test
    void shouldNotAsyncReportCalculationExecuteFourTimes() {
        // Given
        var runnerService = new ReportCalculatorRunnerService(3, 20);

        final AtomicInteger runCounter = new AtomicInteger();

        // When
        runnerService.executeAsyncReportCalculation(() -> runCounter.incrementAndGet() == 4);

        // Then
        then(runCounter.get()).isEqualTo(3);
    }
}