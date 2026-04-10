package org.example.demo.services;

import jakarta.validation.Valid;
import org.example.demo.dto.ProductDTO;
import org.example.demo.dto.request.ProductQueryRequest;
import org.example.demo.dto.response.ProductPageResponse;
import org.example.demo.entities.Product;
import org.example.demo.i18n.Translator;
import org.example.demo.mappers.ProductMapper;
import org.example.demo.repositories.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final Translator translator;

    public ProductService(ProductRepository productRepository, ProductMapper productMapper, Translator translator) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
        this.translator = translator;
    }

    public ProductPageResponse getProductPage(@Valid ProductQueryRequest request) {
        String[] sortParts = request.sort().split(",");
        String sortBy = sortParts[0];
        Sort.Direction sortDirection = sortParts[1].equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(request.page(), request.size(), Sort.by(sortDirection, sortBy));

        Specification<Product> spec = (root, query, cb) -> {
            if (request.search() == null || request.search().isEmpty()) {
                return cb.conjunction();
            }

            String searchPattern = "%" + request.search().toLowerCase() + "%";
            return cb.like(cb.lower(root.get("name")), searchPattern);
        };

        Page<Product> productPage = productRepository.findAll(spec, pageable);

        List<ProductDTO> content = productPage.getContent().stream()
                .map(productMapper::toDTO)
                .toList();

        return toResponse(productPage, content);
    }

    public String fakeData() {
        Random random = new Random();
        List<Product> products = new ArrayList<>();

        for (int i = 0; i < 50; i++) {
            int suffix = random.nextInt(1000);

            Product product = Product.builder()
                    .name(translator.get("product.seed.name", i, suffix))
                    .description(translator.get("product.seed.description", i))
                    .price(new BigDecimal(random.nextInt(1000) * 1000))
                    .quantity(random.nextInt(50) + 1)
                    .active(random.nextBoolean())
                    .createdAt(LocalDateTime.now().minusDays(random.nextInt(30)))
                    .updatedAt(LocalDateTime.now())
                    .build();

            products.add(product);
        }

        productRepository.saveAll(products);
        return translator.get("product.seed.success");
    }

    private ProductPageResponse toResponse(Page<Product> productPage, List<ProductDTO> content) {
        return new ProductPageResponse(
                content,
                productPage.getNumber(),
                productPage.getSize(),
                productPage.getTotalElements(),
                productPage.getTotalPages()
        );
    }
}
