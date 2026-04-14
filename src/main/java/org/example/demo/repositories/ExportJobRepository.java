package org.example.demo.repositories;

import org.example.demo.entities.ExportJob;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExportJobRepository extends JpaRepository<ExportJob, Long> {
}
