package org.example.demo.service;

import org.example.demo.dto.DataTableResponse;
import org.example.demo.dto.PageResponse;

public interface IDataTableService {
    PageResponse<DataTableResponse> getDataTables(int page, int size, String keyword, String sort);

    long countDataTables(String keyword);
}
