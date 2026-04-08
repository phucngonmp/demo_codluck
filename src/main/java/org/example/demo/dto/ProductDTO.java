package org.example.demo.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductDTO(
        Long id,
        String name,
        BigDecimal price,
        String description,
        Integer quantity,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
