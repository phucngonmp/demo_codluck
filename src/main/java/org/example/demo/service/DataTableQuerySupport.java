package org.example.demo.service;

import jakarta.persistence.criteria.Expression;
import org.example.demo.entities.DataTable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Locale;
import java.util.Set;

@Component
public class DataTableQuerySupport {
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "id",
            "renderingEngine",
            "browser",
            "platforms",
            "engineVersion",
            "cssGrade",
            "createdAt"
    );

    public Specification<DataTable> buildKeywordSpecification(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();
        }

        String normalizedKeyword = "%" + keyword.trim().toLowerCase(Locale.ROOT) + "%";

        return (root, query, criteriaBuilder) -> {
            Expression<String> renderingEngine = criteriaBuilder.lower(root.get("renderingEngine"));
            Expression<String> browser = criteriaBuilder.lower(root.get("browser"));
            Expression<String> platforms = criteriaBuilder.lower(root.get("platforms"));
            Expression<String> engineVersion = criteriaBuilder.lower(root.get("engineVersion"));
            Expression<String> cssGrade = criteriaBuilder.lower(root.get("cssGrade"));

            return criteriaBuilder.or(
                    criteriaBuilder.like(renderingEngine, normalizedKeyword),
                    criteriaBuilder.like(browser, normalizedKeyword),
                    criteriaBuilder.like(platforms, normalizedKeyword),
                    criteriaBuilder.like(engineVersion, normalizedKeyword),
                    criteriaBuilder.like(cssGrade, normalizedKeyword)
            );
        };
    }

    public Sort parseSort(String sort) {
        if (!StringUtils.hasText(sort)) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }

        String[] sortParts = sort.split(",");
        String sortField = sortParts[0].trim();
        if (!ALLOWED_SORT_FIELDS.contains(sortField)) {
            sortField = "createdAt";
        }

        Sort.Direction direction = Sort.Direction.ASC;
        if (sortParts.length > 1 && "desc".equalsIgnoreCase(sortParts[1].trim())) {
            direction = Sort.Direction.DESC;
        }

        return Sort.by(direction, sortField);
    }
}
