package org.example.demo.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;

public record ProductExportRequest(
        @Min(value = 100, message = "{validation.export.chunkSize.min}")
        @Max(value = 5000, message = "{validation.export.chunkSize.max}")
        Integer chunkSize,

        @Pattern(regexp = "^(CSV|PDF)$", message = "{validation.export.fileFormat.invalid}")
        String fileFormat
) {
    public ProductExportRequest {
        if (chunkSize == null) {
            chunkSize = 1000;
        }
        if (fileFormat == null || fileFormat.isBlank()) {
            fileFormat = "CSV";
        } else {
            fileFormat = fileFormat.toUpperCase();
        }
    }
}
