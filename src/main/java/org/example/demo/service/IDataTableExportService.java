package org.example.demo.service;

import org.example.demo.dto.DataTableExportJobResponse;
import org.example.demo.entities.ExportFormat;
import org.springframework.core.io.Resource;

public interface IDataTableExportService {
    DataTableExportJobResponse createExportJob(ExportFormat format, String keyword, String sort, String lang);

    DataTableExportJobResponse getExportJob(String jobId);

    Resource loadExportedFile(String jobId);
}
