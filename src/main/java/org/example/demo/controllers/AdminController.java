package org.example.demo.controllers;

import org.example.demo.dto.ApiResponse;
import org.example.demo.service.II18nService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final II18nService i18nService;

    public AdminController(II18nService i18nService) {
        this.i18nService = i18nService;
    }

    @GetMapping("/ping")
    public ApiResponse<String> ping() {
        return ApiResponse.<String>builder()
                .message(i18nService.getMessage("admin.accessGranted"))
                .data(i18nService.getMessage("admin.pong"))
                .build();
    }
}
