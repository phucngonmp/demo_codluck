package org.example.demo.service.impl;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class DataTableExportAsyncProcessor {
    private final DataTableExportService dataTableExportService;

    public DataTableExportAsyncProcessor(DataTableExportService dataTableExportService) {
        this.dataTableExportService = dataTableExportService;
    }

    @Async("dataTableTaskExecutor")
    public void processExportAsync(String jobId) {
        dataTableExportService.processExportAsync(jobId);
    }
}
