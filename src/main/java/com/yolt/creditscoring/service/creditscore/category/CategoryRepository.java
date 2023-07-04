package com.yolt.creditscoring.service.creditscore.category;

import com.yolt.creditscoring.service.creditscore.model.CategorizedAmountEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface CategoryRepository extends CrudRepository<CategorizedAmountEntity, UUID> {

    @Query(value = "select cat.category as category, sum(cat.amount) as totalAmount, sum(cat.transaction_total) as transactionTotal, sum(cat.amount)/sum(cat.transaction_total) as averagePerTransaction from credit_score_monthly_category_report cat " +
            "join credit_score_monthly_report csmr on cat.credit_score_monthly_report_id = csmr.id " +
            "join credit_score_report csr on csmr.credit_score_report_id = csr.id " +
            "join credit_score_user csu on csr.credit_score_user_id = csu.id " +
            "WHERE csu.id = ? " +
            "AND to_date(concat(to_char(csmr.year, '9999'), '-', to_char(csmr.month, '99'), '-01'), 'YYYY-MM-DD') BETWEEN ? AND ? " +
            "group by cat.category", nativeQuery = true)
    List<@Valid GroupedUserCategories> fetchGroupedCategoriesForUser(UUID userId, LocalDate begin, LocalDate end);
}
