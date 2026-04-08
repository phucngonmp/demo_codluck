package org.example.demo.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;

public record ProductQueryRequest(
        @Min(0) Integer page,
        @Min(1) @Max(100) Integer size,
        @Pattern(
                regexp = "^(id|name|price|quantity|createdAt|updatedAt),(asc|desc)$",
                message = "Định dạng sắp xếp không hợp lệ (ví dụ: 'name,asc' hoặc 'price,desc')"
        )
        String sort,
        String search
) {
    // Custom constructor để gán default value nếu cần
    public ProductQueryRequest {
        if (page == null) page = 0;
        if (size == null) size = 10;
        if (sort == null) sort = "createdAt,desc";
    }
}
