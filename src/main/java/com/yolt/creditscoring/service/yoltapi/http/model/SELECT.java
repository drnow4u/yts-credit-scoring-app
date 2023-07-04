package com.yolt.creditscoring.service.yoltapi.http.model;

import lombok.Data;

import java.util.List;

@Data
public class SELECT extends FormComponentDTO {

    private SelectOptionValue defaultValue;

    private String displayName;

    private FieldTypeEnum fieldType;

    private String id;

    private Integer length;

    private Integer maxLength;

    private Boolean optional;

    private List<SelectOptionValueDTO> selectOptionValues;

    private String type;
}
