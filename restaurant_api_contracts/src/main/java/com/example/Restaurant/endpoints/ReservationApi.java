package com.example.Restaurant.endpoints;

import com.example.Restaurant.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "reservations", description = "API для работы с бронированиями")
@RequestMapping("/api/reservations")
public interface ReservationApi {

    @Operation(summary = "Получить бронирование по ID")
    @ApiResponse(responseCode = "200", description = "Бронирование найдено")
    @ApiResponse(responseCode = "404", description = "Бронирование не найдено", content = @Content(schema = @Schema(implementation = StatusResponse.class)))
    @GetMapping("/{id}")
    EntityModel<ReservationResponse> getReservationById(@PathVariable("id") Long id);



    @Operation(summary = "Получить список всех бронирований с фильтрацией и пагинацией")
    @ApiResponse(responseCode = "200", description = "Список бронирований")
    @GetMapping
    PagedModel<EntityModel<ReservationResponse>> getAllReservations(
            @Parameter(description = "Фильтр по ID клиента") @RequestParam(required = false) Long clientId,
            @Parameter(description = "Фильтр по ID столика") @RequestParam(required = false) Long tableId,
            @Parameter(description = "Фильтр по статусу столика") @RequestParam(required = false) String status,
            @Parameter(description = "Фильтр по типу столика") @RequestParam(required = false) String tableType,
            @Parameter(description = "Номер страницы (0..N)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Размер страницы") @RequestParam(defaultValue = "10") int size
    );



    @Operation(summary = "Создать новое бронирование")
    @ApiResponse(responseCode = "201", description = "Бронирование успешно создано")
    @ApiResponse(responseCode = "400", description = "Невалидный запрос", content = @Content(schema = @Schema(implementation = StatusResponse.class)))
    @ApiResponse(responseCode = "409", description = "Столик недоступен в указанное время", content = @Content(schema = @Schema(implementation = StatusResponse.class)))
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    ResponseEntity<EntityModel<ReservationResponse>> createReservation(@Valid @RequestBody ReservationRequest request);

    @Operation(summary = "Обновить бронирование по ID")
    @ApiResponse(responseCode = "200", description = "Бронирование успешно обновлено")
    @ApiResponse(responseCode = "404", description = "Бронирование не найдено", content = @Content(schema = @Schema(implementation = StatusResponse.class)))
    @ApiResponse(responseCode = "409", description = "Столик недоступен в указанное время", content = @Content(schema = @Schema(implementation = StatusResponse.class)))
    @PutMapping("/{id}")
    EntityModel<ReservationResponse> updateReservation(@PathVariable Long id, @Valid @RequestBody UpdateReservationRequest request);

    @Operation(summary = "Удалить бронирование по ID")
    @ApiResponse(responseCode = "204", description = "Бронирование успешно удалено")
    @ApiResponse(responseCode = "404", description = "Бронирование не найдено")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteReservation(@PathVariable Long id);
}