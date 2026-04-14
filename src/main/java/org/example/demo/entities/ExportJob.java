package org.example.demo.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.demo.entities.enums.ExportJobStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "export_jobs")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExportJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ExportJobStatus status;

    @Column(nullable = false, length = 30)
    private String exportType;

    @Column(nullable = false, length = 20)
    private String fileFormat;

    @Column(nullable = false, length = 100)
    private String requestedBy;

    @Column(nullable = false)
    private Integer chunkSize;

    @Column(nullable = false)
    private Long totalRecords;

    @Column(nullable = false)
    private Long processedRecords;

    @Column(length = 255)
    private String fileName;

    @Column(length = 500)
    private String filePath;

    @Column(length = 1000)
    private String errorMessage;

    private LocalDateTime createdAt;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
