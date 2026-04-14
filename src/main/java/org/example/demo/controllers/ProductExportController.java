package org.example.demo.controllers;

import jakarta.validation.Valid;
import org.example.demo.common.ApiResponse;
import org.example.demo.dto.request.ProductExportRequest;
import org.example.demo.dto.response.ExportJobResponse;
import org.example.demo.i18n.Translator;
import org.example.demo.services.IProductExportService;
import org.example.demo.support.ExportAccessContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/exports")
public class ProductExportController {
    private final IProductExportService productExportService;
    private final Translator translator;

    public ProductExportController(IProductExportService productExportService, Translator translator) {
        this.productExportService = productExportService;
        this.translator = translator;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ApiResponse<ExportJobResponse> exportProducts(
            @RequestBody @Valid ProductExportRequest request,
            Authentication authentication
    ) {
        ExportJobResponse response = productExportService.createExportJob(request, toAccessContext(authentication));
        return ApiResponse.<ExportJobResponse>builder()
                .success(true)
                .code(202)
                .message(translator.get("export.job.processing"))
                .data(response)
                .build();
    }

    @GetMapping("/{jobId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ApiResponse<ExportJobResponse> getExportJob(@PathVariable Long jobId, Authentication authentication) {
        return ApiResponse.success(productExportService.getJobResponse(jobId, toAccessContext(authentication)));
    }

    @GetMapping("/{jobId}/download")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<FileSystemResource> downloadExport(@PathVariable Long jobId, Authentication authentication) {
        ExportAccessContext accessContext = toAccessContext(authentication);
        FileSystemResource resource = productExportService.getDownloadResource(jobId, accessContext);
        String fileName = productExportService.getDownloadFileName(jobId, accessContext);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentType(resolveContentType(fileName))
                .body(resource);
    }

    private MediaType resolveContentType(String fileName) {
        if (fileName.toLowerCase().endsWith(".pdf")) {
            return MediaType.APPLICATION_PDF;
        }
        return new MediaType("text", "csv");
    }

    private ExportAccessContext toAccessContext(Authentication authentication) {
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
        return new ExportAccessContext(authentication.getName(), isAdmin);
    }
}
