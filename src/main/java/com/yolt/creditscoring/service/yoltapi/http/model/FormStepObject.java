package com.yolt.creditscoring.service.yoltapi.http.model;

import lombok.Data;

import java.util.List;

@Data
public class FormStepObject {

    private String stateId;

    private List<FormComponentDTO> formComponents;
}
