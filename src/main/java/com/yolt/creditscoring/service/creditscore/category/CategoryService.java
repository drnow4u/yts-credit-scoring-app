package com.yolt.creditscoring.service.creditscore.category;

import com.yolt.creditscoring.service.creditscore.model.Category;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    /**
     * @param begin date is included (closed interval)
     * @param end   date is included (closed interval)
     */
    public Map<Category, SMECategoryDTO> getCategoriesForUser(UUID userId, LocalDate begin, LocalDate end) {

        List<GroupedUserCategories> groupedUserCategories = categoryRepository.fetchGroupedCategoriesForUser(userId, begin, end);

        return groupedUserCategories.stream()
                .map(groupedUserCategory -> SMECategoryDTO.builder()
                        .categoryName(groupedUserCategory.getCategory())
                        .categoryType(groupedUserCategory.getCategory().getSmeCategoryType())
                        .totalTransactionAmount(groupedUserCategory.getTotalAmount())
                        .totalTransactions(groupedUserCategory.getTransactionTotal())
                        .averageTransactionAmount(BigDecimal.valueOf((long) (100 * groupedUserCategory.getAveragePerTransaction()), 2))
                        .build())
                .collect(Collectors.toMap(SMECategoryDTO::getCategoryName, Function.identity()));
    }
}
