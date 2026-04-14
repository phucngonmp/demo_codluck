package org.example.demo.services.impl;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.extern.slf4j.Slf4j;
import org.example.demo.entities.ExportJob;
import org.example.demo.entities.Product;
import org.example.demo.entities.enums.ExportJobStatus;
import org.example.demo.repositories.ExportJobRepository;
import org.example.demo.repositories.ProductRepository;
import org.example.demo.services.IProductExportAsyncService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Slf4j
public class ProductExportAsyncService implements IProductExportAsyncService {
    private static final DateTimeFormatter FILE_TIME_FORMAT = DateTimeFormatter.ofPattern("ddMMyyyyHHmmss");

    private final ExportJobRepository exportJobRepository;
    private final ProductRepository productRepository;

    @Value("${app.export.directory:exports}")
    private String exportDirectory;

    public ProductExportAsyncService(ExportJobRepository exportJobRepository, ProductRepository productRepository) {
        this.exportJobRepository = exportJobRepository;
        this.productRepository = productRepository;
    }

    @Override
    @Async("exportTaskExecutor")
    public void processProductExport(Long jobId) {
        ExportJob job = exportJobRepository.findById(jobId).orElseThrow();
        Path exportPath = null;

        try {
            job.setStatus(ExportJobStatus.PROCESSING);
            job.setStartedAt(LocalDateTime.now());
            exportJobRepository.save(job);

            exportPath = createExportPath(job);
            Files.createDirectories(exportPath.getParent());
            if ("PDF".equalsIgnoreCase(job.getFileFormat())) {
                exportPdf(job, exportPath);
            } else {
                exportCsv(job, exportPath);
            }

            job.setStatus(ExportJobStatus.COMPLETED);
            job.setFinishedAt(LocalDateTime.now());
            job.setFileName(exportPath.getFileName().toString());
            job.setFilePath(exportPath.toAbsolutePath().toString());
            exportJobRepository.save(job);
        } catch (Exception exception) {
            log.error("Product export failed for job {}", jobId, exception);
            job.setStatus(ExportJobStatus.FAILED);
            job.setFinishedAt(LocalDateTime.now());
            job.setErrorMessage(exception.getMessage());
            exportJobRepository.save(job);

            if (exportPath != null) {
                try {
                    Files.deleteIfExists(exportPath);
                } catch (IOException ioException) {
                    log.warn("Failed to delete incomplete export file for job {}", jobId, ioException);
                }
            }
        }
    }

    @Override
    public FileSystemResource getFileResource(ExportJob job) {
        return new FileSystemResource(job.getFilePath());
    }

    private Path createExportPath(ExportJob job) {
        String extension = "PDF".equalsIgnoreCase(job.getFileFormat()) ? ".pdf" : ".csv";
        String fileName = "products-export-" + job.getId() + "-" + FILE_TIME_FORMAT.format(LocalDateTime.now()) + extension;
        return Paths.get(exportDirectory, fileName);
    }

    private void exportCsv(ExportJob job, Path exportPath) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(exportPath, StandardCharsets.UTF_8)) {
            writer.write("id,name,description,price,quantity,active,createdAt,updatedAt");
            writer.newLine();

            exportInChunks(job, chunk -> {
                for (Product product : chunk) {
                    writer.write(toCsvLine(product));
                    writer.newLine();
                }
                writer.flush();
            });
        }
    }

    private void exportPdf(ExportJob job, Path exportPath) throws IOException, DocumentException {
        try (OutputStream outputStream = Files.newOutputStream(exportPath)) {
            Document document = new Document(PageSize.A4.rotate(), 20, 20, 20, 20);
            PdfWriter.getInstance(document, outputStream);
            document.open();
            document.add(new Paragraph("Products Export", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)));
            document.add(new Paragraph(" "));

            exportInChunks(job, chunk -> document.add(createPdfTable(chunk)));

            document.close();
        }
    }

    private void exportInChunks(ExportJob job, ChunkWriter chunkWriter) throws IOException, DocumentException {
        long processedRecords = 0L;
        long lastId = 0L;

        while (true) {
            List<Product> chunk = productRepository.findByIdGreaterThanOrderByIdAsc(
                    lastId,
                    PageRequest.of(0, job.getChunkSize())
            );

            if (chunk.isEmpty()) {
                break;
            }

            chunkWriter.write(chunk);

            lastId = chunk.get(chunk.size() - 1).getId();
            processedRecords += chunk.size();
            job.setProcessedRecords(processedRecords);
            exportJobRepository.save(job);
        }
    }

    private PdfPTable createPdfTable(List<Product> products) {
        PdfPTable table = new PdfPTable(8);
        table.setWidthPercentage(100);
        table.setSpacingAfter(10f);

        addPdfHeader(table, "ID");
        addPdfHeader(table, "Name");
        addPdfHeader(table, "Description");
        addPdfHeader(table, "Price");
        addPdfHeader(table, "Quantity");
        addPdfHeader(table, "Active");
        addPdfHeader(table, "Created At");
        addPdfHeader(table, "Updated At");

        for (Product product : products) {
            addPdfCell(table, product.getId());
            addPdfCell(table, product.getName());
            addPdfCell(table, product.getDescription());
            addPdfCell(table, product.getPrice());
            addPdfCell(table, product.getQuantity());
            addPdfCell(table, product.getActive());
            addPdfCell(table, product.getCreatedAt());
            addPdfCell(table, product.getUpdatedAt());
        }

        return table;
    }

    private void addPdfHeader(PdfPTable table, String value) {
        PdfPCell cell = new PdfPCell(new Phrase(value, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10)));
        table.addCell(cell);
    }

    private void addPdfCell(PdfPTable table, Object value) {
        table.addCell(new Phrase(value == null ? "" : value.toString(), FontFactory.getFont(FontFactory.HELVETICA, 9)));
    }

    private String toCsvLine(Product product) {
        return String.join(",",
                csv(product.getId()),
                csv(product.getName()),
                csv(product.getDescription()),
                csv(product.getPrice()),
                csv(product.getQuantity()),
                csv(product.getActive()),
                csv(product.getCreatedAt()),
                csv(product.getUpdatedAt())
        );
    }

    private String csv(Object value) {
        String text = value == null ? "" : value.toString();
        String escaped = text.replace("\"", "\"\"");
        return "\"" + escaped + "\"";
    }

    @FunctionalInterface
    private interface ChunkWriter {
        void write(List<Product> chunk) throws IOException, DocumentException;
    }
}
