package org.example.demo.services.impl;

import jakarta.validation.Valid;
import org.example.demo.dto.ProductDTO;
import org.example.demo.dto.request.ProductQueryRequest;
import org.example.demo.dto.response.ProductPageResponse;
import org.example.demo.entities.Product;
import org.example.demo.i18n.Translator;
import org.example.demo.mappers.ProductMapper;
import org.example.demo.repositories.ProductRepository;
import org.example.demo.services.IProductService;
import org.example.demo.support.ProductQuerySupport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class ProductService implements IProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final ProductQuerySupport productQuerySupport;
    private final Translator translator;

    public ProductService(
            ProductRepository productRepository,
            ProductMapper productMapper,
            ProductQuerySupport productQuerySupport,
            Translator translator
    ) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
        this.productQuerySupport = productQuerySupport;
        this.translator = translator;
    }

    @Override
    public ProductPageResponse getProductPage(@Valid ProductQueryRequest request) {
        Pageable pageable = productQuerySupport.buildPageable(request);

        Page<Product> productPage = productRepository.findAll(productQuerySupport.buildSpecification(request), pageable);

        List<ProductDTO> content = productPage.getContent().stream()
                .map(productMapper::toDTO)
                .toList();

        return toResponse(productPage, content);
    }

    @Override
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
