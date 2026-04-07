package org.example.demo.controllers;

import org.example.demo.common.ApiResponse;
import org.example.demo.dto.response.AuthResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {
    @GetMapping("/user")
    @PreAuthorize("hasRole('USER')")
    public ApiResponse getUser() {
        System.out.println("here");
        return ApiResponse.success("oke");
    }
}
