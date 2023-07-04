package com.yolt.creditscoring.service.creditscore.storage.dto.response.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.yolt.creditscoring.common.signature.SignatureCreditScoreMonthlyReport;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class MonthlyUserReportDTO implements SignatureCreditScoreMonthlyReport {

    private Integer month;

    private Integer year;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal highestBalance;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal lowestBalance;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal totalIncoming;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal totalOutgoing;

    private Integer incomingTransactionsSize;

    private Integer outgoingTransactionsSize;
}
