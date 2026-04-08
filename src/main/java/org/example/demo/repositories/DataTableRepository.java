package org.example.demo.repositories;

import org.example.demo.entities.DataTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface DataTableRepository extends JpaRepository<DataTable, Long>, JpaSpecificationExecutor<DataTable> {
}
