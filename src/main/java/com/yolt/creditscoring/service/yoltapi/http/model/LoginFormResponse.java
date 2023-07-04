package com.yolt.creditscoring.service.yoltapi.http.model;

import lombok.Data;

import java.util.UUID;

@Data
public class LoginFormResponse {

    private UUID activityId;

    private Step step;

    private UserSite userSite;

    private UUID userSiteId;
}
