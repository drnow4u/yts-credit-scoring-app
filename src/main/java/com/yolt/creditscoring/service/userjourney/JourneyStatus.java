package com.yolt.creditscoring.service.userjourney;

public enum JourneyStatus {
    /**
     * User was invited.
     */
    INVITED,
    /**
     * Invitation expired.
     */
    EXPIRED,
    /**
     * User accept consent.
     */
    CONSENT_ACCEPTED,
    /**
     * User refused consent in the application.
     */
    CONSENT_REFUSED,
    /**
     * User accept consent on bank page.
     */
    BANK_CONSENT_ACCEPTED,
    /**
     * User refused consent on bank page.
     */
    BANK_CONSENT_REFUSED,
    /**
     * User successfully generate his report.
     */
    REPORT_GENERATED,
    /**
     * User sent his report.
     */
    REPORT_SAVED,
    /**
     * User refuse to share his report.
     */
    REPORT_REFUSED,
    /**
     * Unknown error on bank side.
     */
    BANK_ERROR
}
