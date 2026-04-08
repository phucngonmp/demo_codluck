package org.example.demo.dto.response;

import org.example.demo.dto.ProductDTO;

import java.util.List;

public record ProductPageResponse(
        List<ProductDTO> content,
        int pageNumber,
        int pageSize,
        long totalElements,
        int totalPages
) {
}
