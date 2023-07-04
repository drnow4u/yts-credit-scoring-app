package com.yolt.creditscoring.service.userjourney.model;

import com.yolt.creditscoring.service.userjourney.JourneyStatus;

public interface ClientMetricsRowSet {

    int getYear();

    int getMonth();

    JourneyStatus getStatus();

    int getCount();
}
