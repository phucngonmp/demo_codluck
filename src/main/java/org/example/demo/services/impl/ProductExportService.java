package org.example.demo.services.impl;

import org.example.demo.common.ErrorCode;
import org.example.demo.dto.request.ProductExportRequest;
import org.example.demo.dto.response.ExportJobResponse;
import org.example.demo.entities.ExportJob;
import org.example.demo.entities.enums.ExportJobStatus;
import org.example.demo.exception.ClientException;
import org.example.demo.repositories.ExportJobRepository;
import org.example.demo.repositories.ProductRepository;
import org.example.demo.services.IProductExportAsyncService;
import org.example.demo.services.IProductExportService;
import org.example.demo.support.ExportAccessContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Service
public class ProductExportService implements IProductExportService {
    private final ExportJobRepository exportJobRepository;
    private final ProductRepository productRepository;
    private final IProductExportAsyncService productExportAsyncService;

    public ProductExportService(
            ExportJobRepository exportJobRepository,
            ProductRepository productRepository,
            IProductExportAsyncService productExportAsyncService
    ) {
        this.exportJobRepository = exportJobRepository;
        this.productRepository = productRepository;
        this.productExportAsyncService = productExportAsyncService;
    }

    @Override
    public ExportJobResponse createExportJob(ProductExportRequest request, ExportAccessContext accessContext) {
        ExportJob job = ExportJob.builder()
                .status(ExportJobStatus.PENDING)
                .exportType("PRODUCT")
                .fileFormat(request.fileFormat())
                .requestedBy(accessContext.username())
                .chunkSize(request.chunkSize())
                .totalRecords(productRepository.count())
                .processedRecords(0L)
                .build();

        job = exportJobRepository.save(job);
        productExportAsyncService.processProductExport(job.getId());
        return toResponse(job);
    }

    @Override
    public ExportJobResponse getJobResponse(Long jobId, ExportAccessContext accessContext) {
        return toResponse(getAccessibleJob(jobId, accessContext));
    }

    @Override
    public FileSystemResource getDownloadResource(Long jobId, ExportAccessContext accessContext) {
        ExportJob job = getAccessibleJob(jobId, accessContext);
        if (job.getStatus() != ExportJobStatus.COMPLETED || job.getFilePath() == null) {
            throw new ClientException(ErrorCode.EXPORT_JOB_NOT_READY);
        }
        return productExportAsyncService.getFileResource(job);
    }

    @Override
    public String getDownloadFileName(Long jobId, ExportAccessContext accessContext) {
        return getAccessibleJob(jobId, accessContext).getFileName();
    }

    private ExportJob getAccessibleJob(Long jobId, ExportAccessContext accessContext) {
        ExportJob job = exportJobRepository.findById(jobId)
                .orElseThrow(() -> new ClientException(ErrorCode.EXPORT_JOB_NOT_FOUND));

        if (!accessContext.admin() && !job.getRequestedBy().equals(accessContext.username())) {
            throw new ClientException(ErrorCode.EXPORT_ACCESS_DENIED);
        }

        return job;
    }

    private ExportJobResponse toResponse(ExportJob job) {
        int progressPercent = job.getTotalRecords() == 0
                ? 100
                : (int) Math.min(100, (job.getProcessedRecords() * 100) / job.getTotalRecords());

        String downloadUrl = job.getStatus() == ExportJobStatus.COMPLETED
                ? ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/exports/{jobId}/download")
                .buildAndExpand(job.getId())
                .toUriString()
                : null;

        return new ExportJobResponse(
                job.getId(),
                job.getStatus(),
                job.getFileFormat(),
                job.getTotalRecords(),
                job.getProcessedRecords(),
                progressPercent,
                downloadUrl
        );
    }
}
