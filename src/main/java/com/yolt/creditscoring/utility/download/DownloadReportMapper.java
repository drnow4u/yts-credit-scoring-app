package com.yolt.creditscoring.utility.download;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.yolt.creditscoring.service.creditscore.category.SMECategoryDTO;
import com.yolt.creditscoring.service.creditscore.storage.dto.response.admin.MonthlyAdminReportDTO;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

@Slf4j
@UtilityClass
public class DownloadReportMapper {
    private static final String DELIMITER = ";";
    private static final String[] monthsHeaders = {
            "Year",
            "Month",
            "Currency",
            "Highest balance",
            "Lowest balance",
            "Total incoming",
            "# incoming transactions",
            "Total outgoing",
            "# outgoing transactions"
    };
    private static final String[] categoriesHeaders = {
            "Category name",
            "Type",
            "# total transactions",
            "Average transaction amount",
            "Total transaction amount"
    };

    public static byte[] generateCsvForMonths(String currency, Set<MonthlyAdminReportDTO> months) {
        StringBuilder stringBuilder = new StringBuilder(String.join(DELIMITER, monthsHeaders));

        for (MonthlyAdminReportDTO month : months) {
            String[] values = {
                    month.getYear().toString(),
                    month.getMonth().toString(),
                    currency,
                    month.getHighestBalance().toString(),
                    month.getLowestBalance().toString(),
                    month.getTotalIncoming().toString(),
                    month.getIncomingTransactionsSize().toString(),
                    month.getTotalOutgoing().toString(),
                    month.getOutgoingTransactionsSize().toString()
            };

            stringBuilder
                    .append('\n')
                    .append(String.join(DELIMITER, values));
        }

        return stringBuilder.toString().getBytes(StandardCharsets.UTF_8);
    }

    public static byte[] generateCsvForCategories(List<SMECategoryDTO> categories) {
        StringBuilder stringBuilder = new StringBuilder(String.join(DELIMITER, categoriesHeaders));

        for (SMECategoryDTO category : categories) {
            String[] values = {
                    category.getCategoryName().getValue(),
                    category.getCategoryType().name(),
                    category.getTotalTransactions().toString(),
                    category.getAverageTransactionAmount().toString(),
                    category.getTotalTransactionAmount().toString()
            };

            stringBuilder
                    .append('\n')
                    .append(String.join(DELIMITER, values));
        }

        return stringBuilder.toString().getBytes(StandardCharsets.UTF_8);
    }

    public static byte[] generateJson(Object obj) {
        byte[] bytes = null;
        try {
            ObjectMapper objMapper = new ObjectMapper();
            objMapper.registerModule(new JavaTimeModule());
            bytes = objMapper.writeValueAsBytes(obj);
        } catch (JsonProcessingException e) {
            log.warn("Error processing overview .json.");
        }
        return bytes;
    }
}
