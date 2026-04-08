package org.example.demo.config;

import org.example.demo.entities.DataTable;
import org.example.demo.repositories.DataTableRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class DataTableDataInitializer {

    @Bean
    CommandLineRunner seedDataTables(DataTableRepository dataTableRepository) {
        return args -> {
            if (dataTableRepository.count() > 0) {
                return;
            }

            List<DataTable> sampleData = List.of(
                    buildRow("Gecko", "Firefox 1.0", "Win 98+ / OSX.2+", "1.7", "A"),
                    buildRow("Gecko", "Firefox 1.5", "Win 98+ / OSX.2+", "1.8", "A"),
                    buildRow("Gecko", "Firefox 2.0", "Win 98+ / OSX.2+", "1.8", "A"),
                    buildRow("Gecko", "Firefox 3.0", "Win 2k+ / OSX.3+", "1.9", "A"),
                    buildRow("Gecko", "Camino 1.0", "OSX.2+", "1.8", "A"),
                    buildRow("Gecko", "Camino 1.5", "OSX.3+", "1.8", "A"),
                    buildRow("Gecko", "Netscape 7.2", "Win 95+ / Mac OS 8.6-9.2", "1.7", "A"),
                    buildRow("Gecko", "Netscape Browser 8", "Win 98SE+", "1.7", "A"),
                    buildRow("Gecko", "Netscape Navigator 9", "Win 98+ / OSX.2+", "1.8", "A"),
                    buildRow("Gecko", "Mozilla 1.0", "Win 95+ / OSX.1+", "1", "A"),
                    buildRow("Webkit", "Safari 1.2", "OSX.3", "125.5", "A"),
                    buildRow("Webkit", "Safari 1.3", "OSX.3", "312.8", "A"),
                    buildRow("Webkit", "Safari 2.0", "OSX.4+", "419.3", "A"),
                    buildRow("Webkit", "Safari 3.0", "OSX.4+", "522.1", "A"),
                    buildRow("Presto", "Opera 7.0", "Win 95+ / OSX.1+", "-", "A"),
                    buildRow("Presto", "Opera 8.0", "Win 95+ / OSX.1+", "-", "A"),
                    buildRow("Presto", "Opera 9.0", "Win 95+ / OSX.3+", "-", "A"),
                    buildRow("Trident", "Internet Explorer 6", "Win 98+", "6", "A"),
                    buildRow("Trident", "Internet Explorer 7", "Win XP SP2+", "7", "A"),
                    buildRow("Trident", "Internet Explorer 8", "Win XP / Vista", "8", "A")
            );

            dataTableRepository.saveAll(sampleData);
        };
    }

    private DataTable buildRow(String renderingEngine, String browser, String platforms, String engineVersion, String cssGrade) {
        return DataTable.builder()
                .renderingEngine(renderingEngine)
                .browser(browser)
                .platforms(platforms)
                .engineVersion(engineVersion)
                .cssGrade(cssGrade)
                .build();
    }
}
