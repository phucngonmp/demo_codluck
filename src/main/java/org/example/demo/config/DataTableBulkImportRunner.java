package org.example.demo.config;

import org.example.demo.service.IDataTableBulkImportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataTableBulkImportRunner implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(DataTableBulkImportRunner.class);

    private final IDataTableBulkImportService bulkImportService;

    @Value("${app.data-table.bulk-import.enabled:false}")
    private boolean enabled;

    @Value("${app.data-table.bulk-import.count:0}")
    private long count;

    @Value("${app.data-table.bulk-import.batch-size:1000}")
    private int batchSize;

    public DataTableBulkImportRunner(IDataTableBulkImportService bulkImportService) {
        this.bulkImportService = bulkImportService;
    }

    @Override
    public void run(String... args) {
        if (!enabled || count <= 0) {
            return;
        }

        log.info("Starting bulk import for DataTable. count={}, batchSize={}", count, batchSize);
        bulkImportService.importRandomData(count, batchSize);
        log.info("Finished bulk import for DataTable.");
    }
}
