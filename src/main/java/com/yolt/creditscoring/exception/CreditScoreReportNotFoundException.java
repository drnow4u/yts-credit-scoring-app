package com.yolt.creditscoring.exception;

import com.yolt.creditscoring.service.user.model.InvitationStatus;
import lombok.Getter;

public class CreditScoreReportNotFoundException extends RuntimeException {

    @Getter
    InvitationStatus status;

    public CreditScoreReportNotFoundException(InvitationStatus userInvitationStatus, String message) {
        super(message);
        this.status = userInvitationStatus;
    }
}
