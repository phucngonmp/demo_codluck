package org.example.demo.services;

import org.example.demo.dto.response.ProductPageResponse;
import org.example.demo.dto.request.ProductQueryRequest;

public interface IProductService {
    ProductPageResponse getProductPage(ProductQueryRequest request);

    String fakeData();
}
