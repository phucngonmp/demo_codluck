package org.example.demo.service.impl;

import org.example.demo.entities.DataTable;
import org.example.demo.repositories.DataTableRepository;
import org.example.demo.service.IDataTableBulkImportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class DataTableBulkImportService implements IDataTableBulkImportService {
    private static final Logger log = LoggerFactory.getLogger(DataTableBulkImportService.class);

    private static final String[] RENDERING_ENGINES = {"Gecko", "Webkit", "Blink", "Trident", "Presto"};
    private static final String[] BROWSERS = {"Firefox", "Chrome", "Safari", "Opera", "Edge", "Internet Explorer"};
    private static final String[] PLATFORMS = {"Windows", "MacOS", "Linux", "Android", "iOS"};
    private static final String[] CSS_GRADES = {"A", "B", "C", "X"};

    private final DataTableRepository dataTableRepository;
    private final Random random = new Random();

    public DataTableBulkImportService(DataTableRepository dataTableRepository) {
        this.dataTableRepository = dataTableRepository;
    }

    @Override
    public void importRandomData(long count, int batchSize) {
        int safeBatchSize = Math.max(batchSize, 100);
        long remaining = count;

        while (remaining > 0) {
            int currentBatchSize = (int) Math.min(remaining, safeBatchSize);
            saveBatch(currentBatchSize);
            remaining -= currentBatchSize;
            log.info("Bulk import progress: imported={}, remaining={}", count - remaining, remaining);
        }
    }

    @Transactional
    protected void saveBatch(int batchSize) {
        List<DataTable> batch = new ArrayList<>(batchSize);
        for (int i = 0; i < batchSize; i++) {
            batch.add(randomDataTable());
        }
        dataTableRepository.saveAll(batch);
    }

    private DataTable randomDataTable() {
        String browserName = BROWSERS[random.nextInt(BROWSERS.length)];
        int majorVersion = 1 + random.nextInt(120);

        return DataTable.builder()
                .renderingEngine(RENDERING_ENGINES[random.nextInt(RENDERING_ENGINES.length)])
                .browser(browserName + " " + majorVersion + "." + random.nextInt(10))
                .platforms(PLATFORMS[random.nextInt(PLATFORMS.length)] + " / " + PLATFORMS[random.nextInt(PLATFORMS.length)])
                .engineVersion((1 + random.nextInt(10)) + "." + random.nextInt(10))
                .cssGrade(CSS_GRADES[random.nextInt(CSS_GRADES.length)])
                .build();
    }
}
