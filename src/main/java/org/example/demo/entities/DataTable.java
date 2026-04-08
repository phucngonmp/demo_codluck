package org.example.demo.entities;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "data_tables")
public class DataTable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String renderingEngine;

    @Column(nullable = false, length = 150)
    private String browser;

    @Column(nullable = false, length = 150)
    private String platforms;

    @Column(nullable = false, length = 50)
    private String engineVersion;

    @Column(nullable = false, length = 10)
    private String cssGrade;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
