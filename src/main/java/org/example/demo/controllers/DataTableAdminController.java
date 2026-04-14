package org.example.demo.controllers;

import jakarta.validation.Valid;
import org.example.demo.dto.ApiResponse;
import org.example.demo.dto.DataTableExportJobResponse;
import org.example.demo.dto.DataTableExportRequest;
import org.example.demo.entities.ExportFormat;
import org.example.demo.service.II18nService;
import org.example.demo.service.IDataTableExportService;
import org.example.demo.service.impl.DataTableExportAsyncProcessor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/data-tables")
public class DataTableAdminController {
    private final IDataTableExportService exportService;
    private final II18nService i18nService;
    private final DataTableExportAsyncProcessor asyncProcessor;

    public DataTableAdminController(IDataTableExportService exportService, II18nService i18nService, DataTableExportAsyncProcessor asyncProcessor) {
        this.exportService = exportService;
        this.i18nService = i18nService;
        this.asyncProcessor = asyncProcessor;
    }

    @PostMapping("/exports")
    public ApiResponse<DataTableExportJobResponse> createExportJob(
            @Valid @RequestBody DataTableExportRequest request,
            @RequestParam(required = false) String lang
    ) {
        DataTableExportJobResponse job = exportService.createExportJob(
                ExportFormat.valueOf(request.getFormat().toUpperCase()),
                request.getSort(),
                lang
        );
        asyncProcessor.processExportAsync(job.getJobId());

        return ApiResponse.<DataTableExportJobResponse>builder()
                .message(i18nService.getMessage("export.success.started"))
                .data(job)
                .build();
    }

    @GetMapping("/exports/{jobId}")
    public ApiResponse<DataTableExportJobResponse> getExportJob(@PathVariable String jobId) {
        return ApiResponse.<DataTableExportJobResponse>builder()
                .message(i18nService.getMessage("common.success"))
                .data(exportService.getExportJob(jobId))
                .build();
    }

    @GetMapping("/exports/{jobId}/download")
    public ResponseEntity<Resource> downloadExportedFile(@PathVariable String jobId) {
        DataTableExportJobResponse job = exportService.getExportJob(jobId);
        Resource resource = exportService.loadExportedFile(jobId);

        MediaType mediaType = "PDF".equalsIgnoreCase(job.getFormat())
                ? MediaType.APPLICATION_PDF
                : MediaType.parseMediaType("text/csv");

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + job.getFileName() + "\"")
                .body(resource);
    }
}
