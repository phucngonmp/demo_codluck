package org.example.demo.service.impl;

import jakarta.persistence.criteria.Expression;
import org.example.demo.dto.DataTableResponse;
import org.example.demo.dto.PageResponse;
import org.example.demo.entities.DataTable;
import org.example.demo.repositories.DataTableRepository;
import org.example.demo.service.IDataTableService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class DataTableService implements IDataTableService {
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "id",
            "renderingEngine",
            "browser",
            "platforms",
            "engineVersion",
            "cssGrade",
            "createdAt"
    );

    private final DataTableRepository dataTableRepository;

    public DataTableService(DataTableRepository dataTableRepository) {
        this.dataTableRepository = dataTableRepository;
    }

    @Override
    public PageResponse<DataTableResponse> getDataTables(int page, int size, String keyword, String sort) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(size, 1), 100);

        Sort sortConfig = parseSort(sort);
        Pageable pageable = PageRequest.of(safePage - 1, safeSize, sortConfig);

        Page<DataTable> dataTablePage = dataTableRepository.findAll(buildKeywordSpecification(keyword), pageable);

        List<DataTableResponse> content = dataTablePage.getContent().stream()
                .map(this::mapToResponse)
                .toList();

        return PageResponse.<DataTableResponse>builder()
                .content(content)
                .page(safePage)
                .size(safeSize)
                .totalElements(dataTablePage.getTotalElements())
                .totalPages(dataTablePage.getTotalPages())
                .first(dataTablePage.isFirst())
                .last(dataTablePage.isLast())
                .sort(sortConfig.stream()
                        .findFirst()
                        .map(order -> order.getProperty() + "," + order.getDirection().name().toLowerCase(Locale.ROOT))
                        .orElse("createdAt,desc"))
                .build();
    }

    private Specification<DataTable> buildKeywordSpecification(String keyword) {
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

    private Sort parseSort(String sort) {
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

    private DataTableResponse mapToResponse(DataTable dataTable) {
        return DataTableResponse.builder()
                .id(dataTable.getId())
                .renderingEngine(dataTable.getRenderingEngine())
                .browser(dataTable.getBrowser())
                .platforms(dataTable.getPlatforms())
                .engineVersion(dataTable.getEngineVersion())
                .cssGrade(dataTable.getCssGrade())
                .createdAt(dataTable.getCreatedAt())
                .build();
    }
}
