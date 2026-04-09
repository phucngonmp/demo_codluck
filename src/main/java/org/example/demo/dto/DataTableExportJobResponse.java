package org.example.demo.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class DataTableExportJobResponse {
    private String jobId;
    private String format;
    private String status;
    private long totalRecords;
    private long processedRecords;
    private int progressPercent;
    private String fileName;
    private String downloadUrl;
    private String errorMessage;
    private LocalDateTime requestedAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
}
