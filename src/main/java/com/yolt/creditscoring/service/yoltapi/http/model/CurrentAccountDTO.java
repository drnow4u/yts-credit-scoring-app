package com.yolt.creditscoring.service.yoltapi.http.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CurrentAccountDTO {

    private String bic;

    private BigDecimal creditLimit;
}
