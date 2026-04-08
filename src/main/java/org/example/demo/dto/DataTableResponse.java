package org.example.demo.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class DataTableResponse {
    private Long id;
    private String renderingEngine;
    private String browser;
    private String platforms;
    private String engineVersion;
    private String cssGrade;
    private LocalDateTime createdAt;
}
