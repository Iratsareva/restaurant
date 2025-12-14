package com.example.Restaurant.endpoints;

import com.example.Restaurant.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@Tag(name = "tables", description = "API для работы со столиками")
@RequestMapping("/api/tables")
public interface TableApi {

    @Operation(summary = "Получить список всех столиков")
    @ApiResponse(responseCode = "200", description = "Список столиков")
    @GetMapping
    CollectionModel<EntityModel<TableResponse>> getAllTables();



    @Operation(summary = "Получить столик по ID")
    @ApiResponse(responseCode = "200", description = "Столик найден")
    @ApiResponse(responseCode = "404", description = "Столик не найден", content = @Content(schema = @Schema(implementation = StatusResponse.class)))
    @GetMapping("/{id}")
    EntityModel<TableResponse> getTableById(@PathVariable Long id);



    @Operation(summary = "Создать новый столик")
    @ApiResponse(responseCode = "201", description = "Столик успешно создан")
    @ApiResponse(responseCode = "400", description = "Невалидный запрос", content = @Content(schema = @Schema(implementation = StatusResponse.class)))
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    ResponseEntity<EntityModel<TableResponse>> createTable(@Valid @RequestBody TableRequest request);




    @Operation(summary = "Обновить столик")
    @ApiResponse(responseCode = "200", description = "Столик обновлен")
    @ApiResponse(responseCode = "404", description = "Столик не найден", content = @Content(schema = @Schema(implementation = StatusResponse.class)))
    @PutMapping("/{id}")
    EntityModel<TableResponse> updateTable(@PathVariable Long id, @RequestBody @Valid TableRequest request);




    @Operation(summary = "Удалить столик")
    @ApiResponse(responseCode = "204", description = "Столик удален")
    @ApiResponse(responseCode = "404", description = "Столик не найден")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteTable(@PathVariable Long id);
}