package com.yolt.creditscoring.service.yoltapi.http.model;

import lombok.Data;

@Data
public class Step {

    private FormStepObject form;

    private RedirectStepObject redirect;
}
