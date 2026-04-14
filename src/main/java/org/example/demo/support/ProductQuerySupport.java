package org.example.demo.support;

import jakarta.persistence.criteria.Expression;
import org.example.demo.dto.request.ProductQueryRequest;
import org.example.demo.entities.Product;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Locale;
import java.util.Set;

@Component
public class ProductQuerySupport {

    public Pageable buildPageable(ProductQueryRequest request) {
        return PageRequest.of(request.page(), request.size(), parseSort(request.sort()));
    }

    public Specification<Product> buildSpecification(ProductQueryRequest request) {
        return buildSearchSpecification(request.search());
    }

    private Sort parseSort(String sort) {
        if (!StringUtils.hasText(sort)) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }

        String[] sortParts = sort.split(",");
        String sortField = sortParts[0].trim();
        Sort.Direction direction = Sort.Direction.ASC;
        if (sortParts.length > 1 && "desc".equalsIgnoreCase(sortParts[1].trim())) {
            direction = Sort.Direction.DESC;
        }

        return Sort.by(direction, sortField);
    }

    private Specification<Product> buildSearchSpecification(String search) {
        if (!StringUtils.hasText(search)) {
            return (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();
        }

        String normalizedKeyword = "%" + search.trim().toLowerCase(Locale.ROOT) + "%";

        return (root, query, criteriaBuilder) -> {
            Expression<String> name = criteriaBuilder.lower(root.get("name"));
            Expression<String> description = criteriaBuilder.lower(root.get("description"));

            return criteriaBuilder.or(
                    criteriaBuilder.like(name, normalizedKeyword),
                    criteriaBuilder.like(description, normalizedKeyword)
            );
        };
    }
}
