package com.yolt.creditscoring.service.yoltapi.http.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreditCardAccountDTO {

    private BigDecimal availableCredit;

    private BigDecimal creditLimit;

    private String linkedAccount;
}
