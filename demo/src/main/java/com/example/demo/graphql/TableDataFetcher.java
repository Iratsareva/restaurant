package com.example.demo.graphql;


import com.example.Restaurant.dto.TableRequest;
import com.example.Restaurant.dto.TableResponse;
import com.example.demo.service.TableService;
import com.netflix.graphql.dgs.*;
import org.springframework.stereotype.Component;

import java.util.List;

@DgsComponent
public class TableDataFetcher {

    private final TableService tableService;

    public TableDataFetcher(TableService tableService) {
        this.tableService = tableService;
    }

    // ---------- QUERIES ----------
    @DgsQuery
    public List<TableResponse> tables(@InputArgument(name = "typeFilter") String typeFilter) {
        // фильтрация по типу (если в сервисе нет метода, можно сделать вручную)
        List<TableResponse> all = tableService.findAll();
        if (typeFilter != null && !typeFilter.isBlank()) {
            return all.stream()
                    .filter(t -> typeFilter.equalsIgnoreCase(t.getType()))
                    .toList();
        }
        return all;
    }

    @DgsQuery
    public TableResponse tableById(@InputArgument Long id) {
        return tableService.findById(id);
    }

    // ---------- MUTATIONS ----------
    @DgsMutation
    public TableResponse createTable(@InputArgument("input") TableRequest input) {
        return tableService.create(input);
    }

    @DgsMutation
    public TableResponse updateTable(@InputArgument Long id, @InputArgument("input") TableRequest input) {
        return tableService.update(id, input);
    }

    @DgsMutation
    public TableResponse toggleTableAvailability(@InputArgument Long id, @InputArgument Boolean available) {
        return tableService.toggleAvailability(id, available);
    }

    @DgsMutation
    public TableResponse updateTableType(@InputArgument Long id, @InputArgument String type) {
        return tableService.updateType(id, type);
    }

    @DgsMutation
    public Long deleteTable(@InputArgument Long id) {
        tableService.delete(id);
        return id;
    }
}
