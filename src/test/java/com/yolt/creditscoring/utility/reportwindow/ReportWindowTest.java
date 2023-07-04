package com.yolt.creditscoring.utility.reportwindow;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static com.yolt.creditscoring.utility.reportwindow.ReportWindow.windowBegin;
import static com.yolt.creditscoring.utility.reportwindow.ReportWindow.windowEnd;
import static org.assertj.core.api.BDDAssertions.then;

class ReportWindowTest {

    @Test
    void shouldCalculateWindowBegin() {
        //Given
        final LocalDate reportFetchTime = LocalDate.parse("2021-01-02");

        //When
        var begin = windowBegin(reportFetchTime);

        //Then
        then(begin).isEqualTo(LocalDate.parse("2020-01-01"));
    }

    @Test
    void shouldCalculateWindowEnd() {
        //Given
        final LocalDate reportFetchTime = LocalDate.parse("2021-01-02");

        //When
        var end = windowEnd(reportFetchTime);

        //Then
        then(end).isEqualTo(LocalDate.parse("2020-12-31"));
    }

}
