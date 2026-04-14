package org.example.demo.services;

import org.example.demo.entities.ExportJob;
import org.springframework.core.io.FileSystemResource;

public interface IProductExportAsyncService {
    void processProductExport(Long jobId);

    FileSystemResource getFileResource(ExportJob job);
}
