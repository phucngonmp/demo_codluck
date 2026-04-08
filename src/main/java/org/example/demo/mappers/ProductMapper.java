package org.example.demo.mappers;

import ch.qos.logback.core.model.ComponentModel;
import org.example.demo.dto.ProductDTO;
import org.example.demo.entities.Product;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    ProductDTO toDTO(Product product);
}
