package org.example.demo.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "data_table_export_jobs")
public class DataTableExportJob {
    @Id
    @Column(length = 36, nullable = false, updatable = false)
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ExportFormat format;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ExportJobStatus status;

    @Column(length = 255)
    private String keyword;

    @Column(length = 100)
    private String sort;

    @Column(nullable = false)
    private long totalRecords;

    @Column(nullable = false)
    private long processedRecords;

    @Column(length = 255)
    private String fileName;

    @Column(length = 500)
    private String filePath;

    @Column(length = 1000)
    private String errorMessage;

    @Column(nullable = false, updatable = false)
    private LocalDateTime requestedAt;

    private LocalDateTime startedAt;

    private LocalDateTime completedAt;
}
