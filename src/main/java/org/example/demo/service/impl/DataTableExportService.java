package org.example.demo.service.impl;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.example.demo.dto.DataTableExportJobResponse;
import org.example.demo.entities.DataTable;
import org.example.demo.entities.DataTableExportJob;
import org.example.demo.entities.ExportFormat;
import org.example.demo.entities.ExportJobStatus;
import org.example.demo.exception.MessageError;
import org.example.demo.repositories.DataTableExportJobRepository;
import org.example.demo.repositories.DataTableRepository;
import org.example.demo.service.DataTableQuerySupport;
import org.example.demo.service.IDataTableExportService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class DataTableExportService implements IDataTableExportService {
    private static final int EXPORT_CHUNK_SIZE = 1000;

    private final DataTableExportJobRepository exportJobRepository;
    private final DataTableRepository dataTableRepository;
    private final DataTableQuerySupport dataTableQuerySupport;

    @Value("${app.data-table.export.dir:exports}")
    private String exportDir;

    public DataTableExportService(
            DataTableExportJobRepository exportJobRepository,
            DataTableRepository dataTableRepository,
            DataTableQuerySupport dataTableQuerySupport
    ) {
        this.exportJobRepository = exportJobRepository;
        this.dataTableRepository = dataTableRepository;
        this.dataTableQuerySupport = dataTableQuerySupport;
    }

    @Override
    @Transactional
    public DataTableExportJobResponse createExportJob(ExportFormat format, String sort, String lang) {
        long totalRecords = dataTableRepository.count();
        String jobId = UUID.randomUUID().toString();
        String fileExtension = format.name().toLowerCase();
        String fileName = "data-table-export-" + jobId + "." + fileExtension;

        DataTableExportJob exportJob = DataTableExportJob.builder()
                .id(jobId)
                .format(format)
                .status(ExportJobStatus.PENDING)
                .keyword(null)
                .sort(sort)
                .totalRecords(totalRecords)
                .processedRecords(0)
                .fileName(fileName)
                .requestedAt(LocalDateTime.now())
                .build();

        exportJobRepository.save(exportJob);

        return mapToResponse(exportJob);
    }

    @Override
    public DataTableExportJobResponse getExportJob(String jobId) {
        DataTableExportJob exportJob = exportJobRepository.findById(jobId)
                .orElseThrow(() -> new MessageError("export.error.jobNotFound"));
        return mapToResponse(exportJob);
    }

    @Override
    public Resource loadExportedFile(String jobId) {
        DataTableExportJob exportJob = exportJobRepository.findById(jobId)
                .orElseThrow(() -> new MessageError("export.error.jobNotFound"));

        if (exportJob.getStatus() != ExportJobStatus.COMPLETED || exportJob.getFilePath() == null) {
            throw new MessageError("export.error.fileNotReady");
        }

        Path path = Paths.get(exportJob.getFilePath());
        if (!Files.exists(path)) {
            throw new MessageError("export.error.fileMissing");
        }

        return new FileSystemResource(path);
    }

    public void processExportAsync(String jobId) {
        DataTableExportJob exportJob = exportJobRepository.findById(jobId)
                .orElseThrow(() -> new MessageError("export.error.jobNotFound"));

        try {
            prepareJobForProcessing(exportJob);
            Path exportPath = prepareExportPath(exportJob.getFileName());
            Sort sort = dataTableQuerySupport.parseSort(exportJob.getSort());

            if (exportJob.getFormat() == ExportFormat.CSV) {
                exportCsv(exportJob, exportPath, sort);
            } else {
                exportPdf(exportJob, exportPath, sort);
            }

            markJobCompleted(exportJob, exportPath);
        } catch (Exception exception) {
            markJobFailed(exportJob, exception);
        }
    }

    private void exportCsv(DataTableExportJob exportJob, Path exportPath, Sort sort) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(exportPath, StandardCharsets.UTF_8);
             CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT
                     .builder()
                     .setHeader("Rendering engine", "Browser", "Platform(s)", "Engine version", "CSS grade", "Created at")
                     .build())) {

            processChunks(exportJob, sort, chunk -> {
                for (DataTable row : chunk) {
                    csvPrinter.printRecord(
                            row.getRenderingEngine(),
                            row.getBrowser(),
                            row.getPlatforms(),
                            row.getEngineVersion(),
                            row.getCssGrade(),
                            row.getCreatedAt()
                    );
                }
                csvPrinter.flush();
            });
        }
    }

    private void exportPdf(DataTableExportJob exportJob, Path exportPath, Sort sort) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PdfContext pdfContext = new PdfContext(document);

            pdfContext.writeLine("Rendering engine | Browser | Platform(s) | Engine version | CSS grade");
            boolean hasRecords = processChunks(exportJob, sort, chunk -> {
                for (DataTable row : chunk) {
                    String line = String.join(" | ",
                            row.getRenderingEngine(),
                            row.getBrowser(),
                            row.getPlatforms(),
                            row.getEngineVersion(),
                            row.getCssGrade()
                    );
                    pdfContext.writeLine(line);
                }
            });

            if (!hasRecords) {
                pdfContext.writeLine("No data found in data_tables.");
            }

            pdfContext.close();
            document.save(exportPath.toFile());
        }
    }

    private boolean processChunks(DataTableExportJob exportJob, Sort sort, ChunkConsumer chunkConsumer) throws IOException {
        int pageNumber = 0;
        boolean hasRecords = false;
        while (true) {
            Page<DataTable> page = dataTableRepository.findAll(PageRequest.of(pageNumber, EXPORT_CHUNK_SIZE, sort));

            List<DataTable> chunk = page.getContent();
            if (chunk.isEmpty()) {
                break;
            }

            hasRecords = true;
            chunkConsumer.accept(chunk);
            updateProgress(exportJob.getId(), chunk.size());

            if (!page.hasNext()) {
                break;
            }
            pageNumber++;
        }
        return hasRecords;
    }

    @Transactional
    protected void prepareJobForProcessing(DataTableExportJob exportJob) {
        exportJob.setStatus(ExportJobStatus.PROCESSING);
        exportJob.setStartedAt(LocalDateTime.now());
        exportJobRepository.save(exportJob);
    }

    @Transactional
    protected void updateProgress(String jobId, int chunkSize) {
        DataTableExportJob exportJob = exportJobRepository.findById(jobId)
                .orElseThrow(() -> new MessageError("export.error.jobNotFound"));
        exportJob.setProcessedRecords(Math.min(exportJob.getProcessedRecords() + chunkSize, exportJob.getTotalRecords()));
        exportJobRepository.save(exportJob);
    }

    @Transactional
    protected void markJobCompleted(DataTableExportJob exportJob, Path exportPath) {
        exportJob.setStatus(ExportJobStatus.COMPLETED);
        exportJob.setProcessedRecords(exportJob.getTotalRecords());
        exportJob.setCompletedAt(LocalDateTime.now());
        exportJob.setFilePath(exportPath.toAbsolutePath().toString());
        exportJobRepository.save(exportJob);
    }

    @Transactional
    protected void markJobFailed(DataTableExportJob exportJob, Exception exception) {
        exportJob.setStatus(ExportJobStatus.FAILED);
        exportJob.setCompletedAt(LocalDateTime.now());
        exportJob.setErrorMessage(exception.getMessage());
        exportJobRepository.save(exportJob);
    }

    private Path prepareExportPath(String fileName) throws IOException {
        Path exportDirectory = Paths.get(exportDir);
        Files.createDirectories(exportDirectory);
        return exportDirectory.resolve(fileName);
    }

    private DataTableExportJobResponse mapToResponse(DataTableExportJob exportJob) {
        int progressPercent = exportJob.getTotalRecords() == 0
                ? (exportJob.getStatus() == ExportJobStatus.COMPLETED ? 100 : 0)
                : (int) ((exportJob.getProcessedRecords() * 100) / exportJob.getTotalRecords());

        return DataTableExportJobResponse.builder()
                .jobId(exportJob.getId())
                .format(exportJob.getFormat().name())
                .status(exportJob.getStatus().name())
                .totalRecords(exportJob.getTotalRecords())
                .processedRecords(exportJob.getProcessedRecords())
                .progressPercent(progressPercent)
                .fileName(exportJob.getFileName())
                .downloadUrl(exportJob.getStatus() == ExportJobStatus.COMPLETED
                        ? "/api/admin/data-tables/exports/" + exportJob.getId() + "/download"
                        : null)
                .errorMessage(exportJob.getErrorMessage())
                .requestedAt(exportJob.getRequestedAt())
                .startedAt(exportJob.getStartedAt())
                .completedAt(exportJob.getCompletedAt())
                .build();
    }

    @FunctionalInterface
    private interface ChunkConsumer {
        void accept(List<DataTable> chunk) throws IOException;
    }

    private static class PdfContext {
        private static final float MARGIN = 50;
        private static final float START_Y = 750;
        private static final float LEADING = 16;
        private static final PDType1Font FONT = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

        private final PDDocument document;
        private PDPageContentStream contentStream;
        private float currentY;

        private PdfContext(PDDocument document) throws IOException {
            this.document = document;
            createNewPage();
        }

        private void writeLine(String text) throws IOException {
            if (currentY <= MARGIN) {
                closeCurrentStream();
                createNewPage();
            }

            contentStream.beginText();
            contentStream.setFont(FONT, 10);
            contentStream.newLineAtOffset(MARGIN, currentY);
            contentStream.showText(safeText(text));
            contentStream.endText();
            currentY -= LEADING;
        }

        private void createNewPage() throws IOException {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            contentStream = new PDPageContentStream(document, page);
            currentY = START_Y;
        }

        private void close() throws IOException {
            closeCurrentStream();
        }

        private void closeCurrentStream() throws IOException {
            if (contentStream != null) {
                contentStream.close();
            }
        }

        private String safeText(String text) {
            return text.replaceAll("[^\\x20-\\x7E]", "?");
        }
    }
}
