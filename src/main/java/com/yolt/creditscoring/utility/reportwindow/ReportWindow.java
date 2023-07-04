package com.yolt.creditscoring.utility.reportwindow;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

public class ReportWindow {
    public static final int TRANSACTION_WINDOWS_MONTHS_LONG = 12;

    /**
     * Calculate the latest date with months full of transactions (booked)
     * The calculation is not taking into account situation when bank provides
     * transaction for less than TRANSACTION_WINDOWS_MONTHS_LONG.
     *
     * @param reportFetchTime
     * @return
     */
    public static LocalDate windowBegin(LocalDate reportFetchTime) {
        return reportFetchTime
                .minusMonths(TRANSACTION_WINDOWS_MONTHS_LONG)
                .with(TemporalAdjusters.firstDayOfMonth());
    }

    /**
     * Calculate the newest date with months full of transactions (booked)
     *
     * @param reportFetchTime
     * @return
     */
    public static LocalDate windowEnd(LocalDate reportFetchTime) {
        return reportFetchTime
                .minusMonths(1)
                .with(TemporalAdjusters.lastDayOfMonth());
    }
}
