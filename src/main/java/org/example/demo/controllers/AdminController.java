package org.example.demo.controllers;

import org.example.demo.common.ApiResponse;
import org.example.demo.dto.ProductDTO;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @GetMapping
    public ApiResponse<String> findAll() {
        return ApiResponse.success("oke");
    }
}
