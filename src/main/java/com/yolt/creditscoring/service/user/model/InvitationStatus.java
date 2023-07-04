package com.yolt.creditscoring.service.user.model;

public enum InvitationStatus {

    /**
     * Initial status when invitation email is send
     */
    INVITED,
    /**
     * Invitation expired. Can be changed back to INVITED if client admin will resend invitation.
     */
    EXPIRED,
    /**
     * User refused consent in the application. Can be reverted to INVITED by the user.
     */
    REFUSED,
    /**
     * User refused consent on bank page. Terminal state.
     */
    REFUSED_BANK_CONSENT,
    /**
     * Unknown error on bank side. Terminal state.
     */
    ERROR_BANK,
    /**
     * PSU select bank account
     */
    ACCOUNT_SELECTED,
    /**
     * User confirm to share report
     */
    REPORT_SHARED,
    /**
     * User successfully create his report and don't want to share that data. Terminal state.
     */
    REPORT_SHARING_REFUSED,
    /**
     * User successfully pass his user journey and create his report. Terminal state.
     */
    COMPLETED,
    /**
     * Error during report calculation, status stops report polling. Terminal state.
     */
    CALCULATION_ERROR
}
