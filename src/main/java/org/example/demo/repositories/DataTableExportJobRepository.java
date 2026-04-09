package org.example.demo.repositories;

import org.example.demo.entities.DataTableExportJob;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DataTableExportJobRepository extends JpaRepository<DataTableExportJob, String> {
}
