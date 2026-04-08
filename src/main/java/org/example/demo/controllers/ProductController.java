package org.example.demo.controllers;

import jakarta.validation.Valid;
import org.example.demo.common.ApiResponse;
import org.example.demo.dto.request.ProductQueryRequest;
import org.example.demo.dto.response.ProductPageResponse;
import org.example.demo.services.ProductService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }


    @GetMapping()
    public ApiResponse<ProductPageResponse> getProducts(@Valid ProductQueryRequest request) {
        return ApiResponse.success(productService.getProductPage(request));
    }


    @PostMapping()
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ApiResponse<ProductPageResponse> fakeData() {
        productService.fakeData();
        return ApiResponse.success("success");
    }
}
