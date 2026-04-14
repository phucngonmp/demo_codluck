package org.example.demo.dto;

import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DataTableExportRequest {
    @Pattern(regexp = "CSV|PDF", flags = Pattern.Flag.CASE_INSENSITIVE, message = "validation.export.format")
    private String format = "CSV";
    private String sort = "createdAt,desc";
}
