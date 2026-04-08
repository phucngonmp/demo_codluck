package org.example.demo.controllers;

import org.example.demo.dto.ApiResponse;
import org.example.demo.dto.DataTableResponse;
import org.example.demo.dto.PageResponse;
import org.example.demo.service.IDataTableService;
import org.example.demo.service.II18nService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/data-tables")
public class DataTableController {
    private final IDataTableService dataTableService;
    private final II18nService i18nService;

    public DataTableController(IDataTableService dataTableService, II18nService i18nService) {
        this.dataTableService = dataTableService;
        this.i18nService = i18nService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<PageResponse<DataTableResponse>> getDataTables(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "createdAt,desc") String sort
    ) {
        return ApiResponse.<PageResponse<DataTableResponse>>builder()
                .message(i18nService.getMessage("common.success"))
                .data(dataTableService.getDataTables(page, size, keyword, sort))
                .build();
    }
}
