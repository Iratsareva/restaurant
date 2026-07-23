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

/**
 * REST API интерфейс для работы с бронированиями столиков.
 * 
 * <p><b>Назначение:</b> Этот интерфейс определяет контракт REST API для управления бронированиями.
 * Реализует паттерн "Contract-First API Design", где интерфейс определяет контракт,
 * а реализация находится в ReservationController.
 * 
 * <p><b>OpenAPI/Swagger:</b> Использует аннотации Swagger/OpenAPI 3.0 для автоматической
 * генерации документации API. Все методы имеют аннотации @Operation с описанием,
 * параметрами и возможными ответами.
 * 
 * <p><b>HATEOAS:</b> Все методы возвращают EntityModel или PagedModel, которые содержат
 * не только данные, но и ссылки на связанные ресурсы (self, client, table, reservations).
 * Это позволяет клиенту навигироваться по API без знания точных URL.
 * 
 * <p><b>Валидация:</b> Использует Jakarta Bean Validation (@Valid) для проверки входных данных.
 * 
 * <p><b>HTTP методы и их назначение:</b>
 * <ul>
 *   <li>GET - получение данных (idempotent, безопасный)</li>
 *   <li>POST - создание нового ресурса</li>
 *   <li>PUT - полное обновление ресурса (idempotent)</li>
 *   <li>PATCH - частичное обновление (изменение статуса)</li>
 *   <li>DELETE - удаление ресурса (idempotent)</li>
 * </ul>
 * 
 * <p><b>Коды ответов HTTP:</b>
 * <ul>
 *   <li>200 OK - успешный запрос</li>
 *   <li>201 Created - ресурс успешно создан</li>
 *   <li>204 No Content - ресурс успешно удален</li>
 *   <li>400 Bad Request - невалидные данные</li>
 *   <li>404 Not Found - ресурс не найден</li>
 *   <li>409 Conflict - конфликт (например, столик уже забронирован)</li>
 * </ul>
 * 
 * <p><b>Пример использования:</b>
 * <pre>{@code
 * // Создание бронирования
 * POST /api/reservations
 * {
 *   "clientId": 1,
 *   "tableId": 5,
 *   "reservationTime": "2024-01-15T19:00:00",
 *   "numberOfGuests": 4
 * }
 * 
 * // Получение всех бронирований с фильтрацией
 * GET /api/reservations?clientId=1&status=PENDING&page=0&size=10
 * }</pre>
 * 
 * @author Restaurant System
 * @version 1.0
 * @see com.example.demo.controllers.ReservationController
 * @see ReservationRequest
 * @see ReservationResponse
 */
@Tag(name = "reservations", description = "API для работы с бронированиями")
@RequestMapping("/api/reservations")
public interface ReservationApi {
    /**
     * Получить информацию о бронировании по его уникальному идентификатору.
     * 
     * <p><b>HTTP метод:</b> GET
     * <p><b>URL:</b> /api/reservations/{id}
     * 
     * <p><b>Описание:</b> Возвращает полную информацию о бронировании, включая данные клиента,
     * столика, время, количество гостей, статус и цену. Ответ содержит HATEOAS ссылки
     * на связанные ресурсы (self, client, table, reservations).
     * 
     * <p><b>Параметры:</b>
     * @param id Уникальный идентификатор бронирования (path variable)
     * 
     * <p><b>Возвращает:</b> EntityModel с данными бронирования и HATEOAS ссылками
     * 
     * <p><b>Исключения:</b>
     * <ul>
     *   <li>404 Not Found - если бронирование с указанным ID не найдено</li>
     * </ul>
     * 
     * <p><b>Пример запроса:</b>
     * <pre>GET /api/reservations/1</pre>
     * 
     * <p><b>Пример ответа:</b>
     * <pre>{@code
     * {
     *   "id": 1,
     *   "client": { "id": 1, "name": "Иван" },
     *   "table": { "id": 5, "number": "T-5" },
     *   "reservationTime": "2024-01-15T19:00:00",
     *   "numberOfGuests": 4,
     *   "status": "PENDING",
     *   "price": 800.0,
     *   "_links": { ... }
     * }
     * }</pre>
     */
    @Operation(summary = "Получить бронирование по ID")
    @ApiResponse(responseCode = "200", description = "Бронирование найдено")
    @ApiResponse(responseCode = "404", description = "Бронирование не найдено", content = @Content(schema = @Schema(implementation = StatusResponse.class)))
    @GetMapping("/{id}")
    EntityModel<ReservationResponse> getReservationById(@PathVariable("id") Long id);

    /**
     * Получить список всех бронирований с поддержкой фильтрации и пагинации.
     * 
     * <p><b>HTTP метод:</b> GET
     * <p><b>URL:</b> /api/reservations
     * 
     * <p><b>Описание:</b> Возвращает страничный список бронирований с возможностью фильтрации
     * по различным критериям. Поддерживает пагинацию для работы с большими объемами данных.
     * 
     * <p><b>Фильтрация:</b> Можно комбинировать несколько фильтров одновременно.
     * Если указано несколько фильтров, применяется логика AND (все условия должны выполняться).
     * 
     * <p><b>Пагинация:</b> Использует стандартную Spring пагинацию с нумерацией страниц с 0.
     * 
     * <p><b>Параметры запроса (все опциональные):</b>
     * @param clientId Фильтр по ID клиента. Если указан, возвращаются только бронирования этого клиента.
     * @param tableId Фильтр по ID столика. Если указан, возвращаются только бронирования этого столика.
     * @param status Фильтр по статусу бронирования (PENDING, CONFIRMED, PAID, CANCELLED).
     * @param tableType Фильтр по типу столика (STANDARD, VIP, WINDOW и т.д.).
     * @param page Номер страницы (начинается с 0). По умолчанию: 0.
     * @param size Размер страницы (количество элементов на странице). По умолчанию: 10.
     * 
     * <p><b>Возвращает:</b> PagedModel с данными бронирований, метаинформацией о пагинации
     * (totalElements, totalPages, last) и HATEOAS ссылками.
     * 
     * <p><b>Примеры запросов:</b>
     * <pre>
     * GET /api/reservations                              // Все бронирования, первая страница
     * GET /api/reservations?clientId=1                   // Бронирования клиента с ID=1
     * GET /api/reservations?status=PENDING&page=0&size=5  // Первая страница из 5 pending бронирований
     * GET /api/reservations?tableType=VIP                 // Бронирования VIP столиков
     * </pre>
     * 
     * <p><b>Пример ответа:</b>
     * <pre>{@code
     * {
     *   "_embedded": {
     *     "reservations": [ ... ]
     *   },
     *   "_links": { ... },
     *   "page": {
     *     "size": 10,
     *     "totalElements": 25,
     *     "totalPages": 3,
     *     "number": 0
     *   }
     * }
     * }</pre>
     */
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

    /**
     * Создать новое бронирование столика.
     * 
     * <p><b>HTTP метод:</b> POST
     * <p><b>URL:</b> /api/reservations
     * 
     * <p><b>Описание:</b> Создает новое бронирование на основе переданных данных.
     * Выполняет валидацию данных, проверку доступности столика, проверку пересечений
     * по времени, расчет цены через gRPC сервис и публикацию события в RabbitMQ.
     * 
     * <p><b>Бизнес-логика при создании:</b>
     * <ol>
     *   <li>Проверка существования клиента и столика</li>
     *   <li>Проверка доступности столика (isAvailable = true)</li>
     *   <li>Проверка, что количество гостей не превышает количество мест</li>
     *   <li>Проверка отсутствия пересечений по времени (бронирование длится 2 часа)</li>
     *   <li>Создание бронирования со статусом PENDING</li>
     *   <li>Синхронный расчет цены через gRPC сервис</li>
     *   <li>Публикация событий в RabbitMQ (ReservationCreatedEvent, ReservationPricedEvent)</li>
     * </ol>
     * 
     * <p><b>Параметры:</b>
     * @param request Объект ReservationRequest с данными для создания бронирования.
     *                Валидируется автоматически через @Valid.
     * 
     * <p><b>Возвращает:</b> ResponseEntity с кодом 201 Created и данными созданного бронирования.
     * В заголовке Location будет URL созданного ресурса.
     * 
     * <p><b>Исключения:</b>
     * <ul>
     *   <li>400 Bad Request - если данные невалидны (не проходят валидацию)</li>
     *   <li>404 Not Found - если клиент или столик не найдены</li>
     *   <li>409 Conflict - если столик недоступен или уже забронирован на это время</li>
     * </ul>
     * 
     * <p><b>Побочные эффекты:</b>
     * <ul>
     *   <li>Создается запись в базе данных</li>
     *   <li>Публикуется событие ReservationCreatedEvent в RabbitMQ</li>
     *   <li>Публикуется событие ReservationPricedEvent в RabbitMQ (после расчета цены)</li>
     *   <li>Аудит-сервис и сервис уведомлений получают события через RabbitMQ</li>
     * </ul>
     * 
     * <p><b>Пример запроса:</b>
     * <pre>{@code
     * POST /api/reservations
     * Content-Type: application/json
     * 
     * {
     *   "clientId": 1,
     *   "tableId": 5,
     *   "reservationTime": "2024-01-15T19:00:00",
     *   "numberOfGuests": 4
     * }
     * }</pre>
     */
    @Operation(summary = "Создать новое бронирование")
    @ApiResponse(responseCode = "201", description = "Бронирование успешно создано")
    @ApiResponse(responseCode = "400", description = "Невалидный запрос", content = @Content(schema = @Schema(implementation = StatusResponse.class)))
    @ApiResponse(responseCode = "409", description = "Столик недоступен в указанное время", content = @Content(schema = @Schema(implementation = StatusResponse.class)))
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    ResponseEntity<EntityModel<ReservationResponse>> createReservation(@Valid @RequestBody ReservationRequest request);

    /**
     * Обновить существующее бронирование (полное обновление).
     * 
     * <p><b>HTTP метод:</b> PUT
     * <p><b>URL:</b> /api/reservations/{id}
     * 
     * <p><b>Описание:</b> Полностью обновляет данные бронирования (время и количество гостей).
     * Это идемпотентная операция - повторный вызов с теми же данными даст тот же результат.
     * 
     * <p><b>Что можно обновить:</b>
     * <ul>
     *   <li>reservationTime - время бронирования</li>
     *   <li>numberOfGuests - количество гостей</li>
     * </ul>
     * 
     * <p><b>Проверки при обновлении:</b>
     * <ul>
     *   <li>Существование бронирования</li>
     *   <li>Количество гостей не превышает количество мест за столом</li>
     *   <li>Отсутствие пересечений по времени (исключая само обновляемое бронирование)</li>
     * </ul>
     * 
     * <p><b>Параметры:</b>
     * @param id Уникальный идентификатор бронирования для обновления (path variable)
     * @param request Объект UpdateReservationRequest с новыми данными (валидируется через @Valid)
     * 
     * <p><b>Возвращает:</b> EntityModel с обновленными данными бронирования
     * 
     * <p><b>Исключения:</b>
     * <ul>
     *   <li>404 Not Found - если бронирование не найдено</li>
     *   <li>409 Conflict - если столик уже забронирован на новое время</li>
     * </ul>
     * 
     * <p><b>Примечание:</b> Статус бронирования обновляется отдельным методом updateReservationStatus.
     * 
     * <p><b>Пример запроса:</b>
     * <pre>{@code
     * PUT /api/reservations/1
     * Content-Type: application/json
     * 
     * {
     *   "reservationTime": "2024-01-15T20:00:00",
     *   "numberOfGuests": 6
     * }
     * }</pre>
     */
    @Operation(summary = "Обновить бронирование по ID")
    @ApiResponse(responseCode = "200", description = "Бронирование успешно обновлено")
    @ApiResponse(responseCode = "404", description = "Бронирование не найдено", content = @Content(schema = @Schema(implementation = StatusResponse.class)))
    @ApiResponse(responseCode = "409", description = "Столик недоступен в указанное время", content = @Content(schema = @Schema(implementation = StatusResponse.class)))
    @PutMapping("/{id}")
    EntityModel<ReservationResponse> updateReservation(@PathVariable Long id, @Valid @RequestBody UpdateReservationRequest request);

    /**
     * Изменить статус бронирования (частичное обновление).
     * 
     * <p><b>HTTP метод:</b> PATCH
     * <p><b>URL:</b> /api/reservations/{id}/status
     * 
     * <p><b>Описание:</b> Изменяет только статус бронирования. Поддерживает строгую модель
     * переходов состояний (state machine) для обеспечения целостности данных.
     * 
     * <p><b>Модель переходов статусов (State Machine):</b>
     * <pre>
     * PENDING → CONFIRMED, PAID, CANCELLED
     * CONFIRMED → PAID, CANCELLED
     * PAID → CANCELLED
     * CANCELLED → (нет переходов, финальное состояние)
     * </pre>
     * 
     * <p><b>Возможные статусы:</b>
     * <ul>
     *   <li><b>PENDING</b> - ожидает подтверждения (начальное состояние при создании)</li>
     *   <li><b>CONFIRMED</b> - подтверждено администратором</li>
     *   <li><b>PAID</b> - оплачено клиентом</li>
     *   <li><b>CANCELLED</b> - отменено (финальное состояние, нельзя вернуть)</li>
     * </ul>
     * 
     * <p><b>Параметры:</b>
     * @param id Уникальный идентификатор бронирования (path variable)
     * @param status Новый статус (query parameter). Должен быть одним из: CONFIRMED, PAID, CANCELLED
     * 
     * <p><b>Возвращает:</b> EntityModel с обновленными данными бронирования
     * 
     * <p><b>Исключения:</b>
     * <ul>
     *   <li>400 Bad Request - если переход статуса недопустим согласно модели переходов</li>
     *   <li>404 Not Found - если бронирование не найдено</li>
     * </ul>
     * 
     * <p><b>Побочные эффекты:</b>
     * <ul>
     *   <li>Публикуется событие ReservationStatusChangedEvent в RabbitMQ</li>
     *   <li>Сервис уведомлений получает событие и отправляет уведомление через WebSocket</li>
     * </ul>
     * 
     * <p><b>Примеры запросов:</b>
     * <pre>
     * PATCH /api/reservations/1/status?status=CONFIRMED  // Подтвердить бронирование
     * PATCH /api/reservations/1/status?status=PAID       // Отметить как оплаченное
     * PATCH /api/reservations/1/status?status=CANCELLED  // Отменить бронирование
     * </pre>
     * 
     * <p><b>Автоматическая отмена:</b> Просроченные PENDING бронирования (старше 24 часов)
     * автоматически отменяются планировщиком ReservationStatusScheduler каждые 6 часов.
     */
    @Operation(summary = "Изменить статус бронирования", 
               description = "Изменяет статус бронирования. Допустимые переходы: PENDING -> CONFIRMED/PAID/CANCELLED, CONFIRMED/PAID -> CANCELLED")
    @ApiResponse(responseCode = "200", description = "Статус успешно изменен")
    @ApiResponse(responseCode = "400", description = "Недопустимый переход статуса", content = @Content(schema = @Schema(implementation = StatusResponse.class)))
    @ApiResponse(responseCode = "404", description = "Бронирование не найдено", content = @Content(schema = @Schema(implementation = StatusResponse.class)))
    @PatchMapping("/{id}/status")
    EntityModel<ReservationResponse> updateReservationStatus(
            @PathVariable("id") Long id,
            @Parameter(description = "Новый статус (CONFIRMED, PAID, CANCELLED)") @RequestParam("status") String status);

    /**
     * Удалить бронирование по идентификатору.
     * 
     * <p><b>HTTP метод:</b> DELETE
     * <p><b>URL:</b> /api/reservations/{id}
     * 
     * <p><b>Описание:</b> Удаляет бронирование из базы данных. Это идемпотентная операция -
     * повторное удаление несуществующего бронирования не вызовет ошибку (но вернет 404).
     * 
     * <p><b>Параметры:</b>
     * @param id Уникальный идентификатор бронирования для удаления (path variable)
     * 
     * <p><b>Возвращает:</b> HTTP 204 No Content (тело ответа пустое)
     * 
     * <p><b>Исключения:</b>
     * <ul>
     *   <li>404 Not Found - если бронирование не найдено</li>
     * </ul>
     * 
     * <p><b>Побочные эффекты:</b>
     * <ul>
     *   <li>Бронирование удаляется из базы данных</li>
     *   <li>Публикуется событие ReservationDeletedEvent в RabbitMQ</li>
     *   <li>Аудит-сервис получает событие и логирует удаление</li>
     *   <li>Сервис уведомлений получает событие и отправляет уведомление через WebSocket</li>
     * </ul>
     * 
     * <p><b>Пример запроса:</b>
     * <pre>DELETE /api/reservations/1</pre>
     * 
     * <p><b>Примечание:</b> После удаления восстановление данных невозможно.
     * Для отмены бронирования рекомендуется использовать изменение статуса на CANCELLED.
     */
    @Operation(summary = "Удалить бронирование по ID")
    @ApiResponse(responseCode = "204", description = "Бронирование успешно удалено")
    @ApiResponse(responseCode = "404", description = "Бронирование не найдено")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteReservation(@PathVariable Long id);
}