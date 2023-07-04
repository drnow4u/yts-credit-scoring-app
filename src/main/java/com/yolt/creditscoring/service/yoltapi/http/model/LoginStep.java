package com.yolt.creditscoring.service.yoltapi.http.model;

import lombok.Data;

import java.util.UUID;

@Data
public class LoginStep {

    private FormStepObject form;

    private RedirectStepObject redirect;

    private UUID userSiteId;
}
