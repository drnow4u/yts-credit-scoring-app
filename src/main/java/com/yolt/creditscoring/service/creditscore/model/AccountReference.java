package com.yolt.creditscoring.service.creditscore.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Embeddable;

@Data
@Builder
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class AccountReference {
    private String bban;
    private String iban;
    private String maskedPan;
    private String sortCodeAccountNumber;
}
