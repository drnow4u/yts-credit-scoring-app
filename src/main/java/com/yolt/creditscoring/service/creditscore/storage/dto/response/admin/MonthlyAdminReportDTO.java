package com.yolt.creditscoring.service.creditscore.storage.dto.response.admin;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.yolt.creditscoring.common.signature.SignatureCreditScoreMonthlyReport;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class MonthlyAdminReportDTO implements SignatureCreditScoreMonthlyReport {

    private Integer year;

    private Integer month;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal highestBalance;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal lowestBalance;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal averageBalance;

    private Integer incomingTransactionsSize;

    private Integer outgoingTransactionsSize;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal totalIncoming;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal totalOutgoing;

}
