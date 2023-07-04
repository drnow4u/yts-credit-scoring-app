package com.yolt.creditscoring.service.yoltapi.http.model;

import lombok.Data;

import java.util.List;

@Data
public class TransactionsPageDTO {

    private String next;

    private List<TransactionDTO> transactions;
}
