package org.example.demo.services;

import jakarta.validation.Valid;
import org.example.demo.dto.ProductDTO;
import org.example.demo.dto.request.ProductQueryRequest;
import org.example.demo.dto.response.ProductPageResponse;
import org.example.demo.entities.Product;
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

    public ProductService(ProductRepository productRepository, ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
    }

    public ProductPageResponse getProductPage(@Valid ProductQueryRequest request) {
        // 1. Xử lý Sort: tách ra sort theo gì và tăng hay giảm
        String[] sortParts = request.sort().split(",");
        String sortBy = sortParts[0];
        Sort.Direction sortDirection = sortParts[1].equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;


        // 2. Tạo đối tượng Pageable
        Pageable pageable = PageRequest.of(request.page(), request.size(), Sort.by(sortDirection, sortBy));

        // 3. Xử lý Search bằng Specification (Tìm trong 'name')
        Specification<Product> spec = (root, query, cb) -> {
            if (request.search() == null || request.search().isEmpty()) {
                return cb.conjunction(); // Không search gì thì trả về toàn bộ
            }
            String searchPattern = "%" + request.search().toLowerCase() + "%";
            return cb.like(cb.lower(root.get("name")), searchPattern
            );
        };

        // 4. Gọi Repository
        Page<Product> productPage = productRepository.findAll(spec, pageable);

        // 5. Map sang Response DTO
        List<ProductDTO> content = productPage.getContent().stream()
                .map(productMapper::toDTO)
                .toList();
        return toResponse(productPage, content);
    }

    public String fakeData() {
        Random random = new Random();
        List<Product> products = new ArrayList<>();

        for (int i = 0; i < 50; i++) { // Tạo hẳn 50 bản ghi để test phân trang cho sướng
            Product product = Product.builder()
                    .name("Sản phẩm " + i + " - " + random.nextInt(1000))
                    .description("Mô tả chi tiết cho sản phẩm số " + i + ". Đây là dữ liệu giả lập.")
                    .price(new BigDecimal(random.nextInt(1000) * 1000)) // Giá từ 0 đến 1.000.000
                    .quantity(random.nextInt(50) + 1)
                    .active(random.nextBoolean())
                    .createdAt(LocalDateTime.now().minusDays(random.nextInt(30))) // Tạo ngày ngẫu nhiên trong 30 ngày qua
                    .updatedAt(LocalDateTime.now())
                    .build();

            products.add(product);
        }

        productRepository.saveAll(products); // Lưu cả list một lần để tối ưu hiệu năng
        return "Đã tạo thành công 50 sản phẩm mẫu!";
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
