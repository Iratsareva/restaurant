package com.example.demo.controllers;




import com.example.Restaurant.dto.TableRequest;
import com.example.Restaurant.dto.TableResponse;
import com.example.Restaurant.endpoints.TableApi;
import com.example.demo.assembler.TableModelAssembler;
import com.example.demo.service.TableService;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
public class TableController implements TableApi {
    private final TableService tableService;
    private final TableModelAssembler assembler;

    public TableController(TableService tableService, TableModelAssembler assembler) {
        this.tableService = tableService;
        this.assembler = assembler;
    }

    @Override
    public CollectionModel<EntityModel<TableResponse>> getAllTables() {
        return assembler.toCollectionModel(tableService.findAll());
    }



    @Override
    public EntityModel<TableResponse> getTableById(Long id) {
        return assembler.toModel(tableService.findById(id));
    }

    @Override
    public ResponseEntity<EntityModel<TableResponse>> createTable(TableRequest request) {
        TableResponse created = tableService.create(request);
        EntityModel<TableResponse> model = assembler.toModel(created);
        return ResponseEntity.created(model.getRequiredLink("self").toUri())
                .body(model);
    }

    @Override
    public EntityModel<TableResponse> updateTable(Long id, TableRequest request) {
        return assembler.toModel(tableService.update(id, request));
    }

    @Override
    public void deleteTable(Long id) {
        tableService.delete(id);
    }
}