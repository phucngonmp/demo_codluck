package org.example.demo.services;

import org.example.demo.dto.request.ProductExportRequest;
import org.example.demo.dto.response.ExportJobResponse;
import org.example.demo.support.ExportAccessContext;
import org.springframework.core.io.FileSystemResource;

public interface IProductExportService {
    ExportJobResponse createExportJob(ProductExportRequest request, ExportAccessContext accessContext);

    ExportJobResponse getJobResponse(Long jobId, ExportAccessContext accessContext);

    FileSystemResource getDownloadResource(Long jobId, ExportAccessContext accessContext);

    String getDownloadFileName(Long jobId, ExportAccessContext accessContext);
}
