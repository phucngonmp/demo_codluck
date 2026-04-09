package org.example.demo.service.impl;

import jakarta.persistence.criteria.Expression;
import org.example.demo.dto.DataTableResponse;
import org.example.demo.dto.PageResponse;
import org.example.demo.entities.DataTable;
import org.example.demo.repositories.DataTableRepository;
import org.example.demo.service.DataTableQuerySupport;
import org.example.demo.service.IDataTableService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
public class DataTableService implements IDataTableService {
    private final DataTableRepository dataTableRepository;
    private final DataTableQuerySupport dataTableQuerySupport;

    public DataTableService(DataTableRepository dataTableRepository, DataTableQuerySupport dataTableQuerySupport) {
        this.dataTableRepository = dataTableRepository;
        this.dataTableQuerySupport = dataTableQuerySupport;
    }

    @Override
    public PageResponse<DataTableResponse> getDataTables(int page, int size, String keyword, String sort) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(size, 1), 100);

        Sort sortConfig = dataTableQuerySupport.parseSort(sort);
        Pageable pageable = PageRequest.of(safePage - 1, safeSize, sortConfig);

        Page<DataTable> dataTablePage = dataTableRepository.findAll(dataTableQuerySupport.buildKeywordSpecification(keyword), pageable);

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

    @Override
    public long countDataTables(String keyword) {
        return dataTableRepository.count(dataTableQuerySupport.buildKeywordSpecification(keyword));
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
