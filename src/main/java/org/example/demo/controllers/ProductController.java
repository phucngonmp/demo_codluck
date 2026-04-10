package org.example.demo.controllers;

import jakarta.validation.Valid;
import org.example.demo.common.ApiResponse;
import org.example.demo.dto.request.ProductQueryRequest;
import org.example.demo.dto.response.ProductPageResponse;
import org.example.demo.i18n.Translator;
import org.example.demo.services.ProductService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("products")
public class ProductController {

    private final ProductService productService;
    private final Translator translator;

    public ProductController(ProductService productService, Translator translator) {
        this.productService = productService;
        this.translator = translator;
    }

    @GetMapping()
    public ApiResponse<ProductPageResponse> getProducts(@Valid ProductQueryRequest request) {
        return ApiResponse.success(productService.getProductPage(request));
    }

    @PostMapping()
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ApiResponse<String> fakeData() {
        productService.fakeData();
        return ApiResponse.success(translator.get("product.seed.success"));
    }
}
