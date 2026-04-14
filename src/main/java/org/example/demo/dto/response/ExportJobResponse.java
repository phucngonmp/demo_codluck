package org.example.demo.dto.response;

import org.example.demo.entities.enums.ExportJobStatus;

public record ExportJobResponse(
        Long jobId,
        ExportJobStatus status,
        String fileFormat,
        long totalRecords,
        long processedRecords,
        int progressPercent,
        String downloadUrl
) {
}
