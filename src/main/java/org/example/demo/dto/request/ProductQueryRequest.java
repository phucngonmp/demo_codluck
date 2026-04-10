package org.example.demo.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;

public record ProductQueryRequest(
        @Min(value = 0, message = "{validation.product.page.min}")
        Integer page,

        @Min(value = 1, message = "{validation.product.size.min}")
        @Max(value = 1000, message = "{validation.product.size.max}")
        Integer size,

        @Pattern(
                regexp = "^(id|name|price|quantity|createdAt|updatedAt),(asc|desc)$",
                message = "{validation.product.sort.invalid}"
        )
        String sort,

        String search
) {
    public ProductQueryRequest {
        if (page == null) {
            page = 0;
        }
        if (size == null) {
            size = 10;
        }
        if (sort == null) {
            sort = "createdAt,desc";
        }
    }
}
