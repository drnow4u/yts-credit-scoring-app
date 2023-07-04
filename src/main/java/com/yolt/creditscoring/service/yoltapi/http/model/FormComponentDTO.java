package com.yolt.creditscoring.service.yoltapi.http.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;


@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = SELECT.class, name = "SELECT")
})
@Data
public class FormComponentDTO {

    private ComponentTypeEnum componentType;

    private String displayName;

    private Boolean optional;

    private String type;
}
