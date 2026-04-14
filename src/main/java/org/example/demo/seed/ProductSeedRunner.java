package org.example.demo.seed;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
public class ProductSeedRunner implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    public ProductSeedRunner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        if (args.length == 0 || !args[0].equals("seed-products")) {
            return;
        }

        int total = args.length > 1 ? Integer.parseInt(args[1]) : 100_000;
        int batchSize = args.length > 2 ? Integer.parseInt(args[2]) : 1_000;

        seedProducts(total, batchSize);
    }

    private void seedProducts(int total, int batchSize) {
        String sql = """
            INSERT INTO product
            (name, description, price, quantity, active, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

        Random random = new Random();
        int inserted = 0;

        while (inserted < total) {
            int currentBatchSize = Math.min(batchSize, total - inserted);
            List<Object[]> batchArgs = new ArrayList<>(currentBatchSize);

            for (int i = 0; i < currentBatchSize; i++) {
                int index = inserted + i + 1;
                LocalDateTime createdAt = LocalDateTime.now().minusDays(random.nextInt(365));

                batchArgs.add(new Object[]{
                        "Product " + index + "-" + random.nextInt(100000),
                        "Random description for product #" + index,
                        BigDecimal.valueOf(10 + (random.nextDouble() * 9990)),
                        random.nextInt(500) + 1,
                        random.nextBoolean(),
                        Timestamp.valueOf(createdAt),
                        Timestamp.valueOf(LocalDateTime.now())
                });
            }

            jdbcTemplate.batchUpdate(sql, batchArgs);
            inserted += currentBatchSize;
            System.out.println("Inserted " + inserted + "/" + total);
        }

        System.out.println("Seed completed.");
    }
}
