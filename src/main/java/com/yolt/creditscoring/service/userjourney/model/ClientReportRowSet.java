package com.yolt.creditscoring.service.userjourney.model;

import com.yolt.creditscoring.service.userjourney.JourneyStatus;

public interface ClientReportRowSet {

    String getClientName();

    JourneyStatus getStatus();

    int getCount();
}
