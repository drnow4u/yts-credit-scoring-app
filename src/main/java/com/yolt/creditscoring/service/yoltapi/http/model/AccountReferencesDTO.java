package com.yolt.creditscoring.service.yoltapi.http.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AccountReferencesDTO {

    @JsonProperty("bban")
    private String bban;

    @JsonProperty("iban")
    private String iban;

    @JsonProperty("maskedPan")
    private String maskedPan;

    @JsonProperty("sortCodeAccountNumber")
    private String sortCodeAccountNumber;
}
