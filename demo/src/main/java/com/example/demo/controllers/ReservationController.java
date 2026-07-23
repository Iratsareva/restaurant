package com.example.demo.controllers;



import com.example.Restaurant.dto.PagedResponse;
import com.example.Restaurant.dto.ReservationRequest;
import com.example.Restaurant.dto.ReservationResponse;
import com.example.Restaurant.dto.UpdateReservationRequest;
import com.example.Restaurant.endpoints.ReservationApi;
import com.example.demo.assembler.ReservationModelAssembler;
import com.example.demo.service.ReservationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

/**
 * REST контроллер для обработки HTTP запросов, связанных с бронированиями.
 * 
 * <p><b>Назначение:</b> Этот класс является реализацией интерфейса ReservationApi и обрабатывает
 * все HTTP запросы к эндпоинтам /api/reservations/*. Следует паттерну MVC (Model-View-Controller),
 * где контроллер отвечает за прием запросов и возврат ответов, делегируя бизнес-логику сервисному слою.
 * 
 * <p><b>Архитектурная роль:</b>
 * <ul>
 *   <li><b>Presentation Layer</b> - слой представления, первый уровень обработки HTTP запросов</li>
 *   <li><b>Request Routing</b> - маршрутизация запросов к соответствующим методам сервиса</li>
 *   <li><b>Response Transformation</b> - преобразование DTO в HATEOAS модели (EntityModel/PagedModel)</li>
 *   <li><b>HTTP Protocol Handling</b> - работа с HTTP методами, статус-кодами, заголовками</li>
 * </ul>
 * 
 * <p><b>Зависимости (Dependency Injection):</b>
 * <ul>
 *   <li><b>ReservationService</b> - бизнес-логика работы с бронированиями</li>
 *   <li><b>ReservationModelAssembler</b> - преобразование ReservationResponse в EntityModel с HATEOAS ссылками</li>
 *   <li><b>PagedResourcesAssembler</b> - преобразование страничных данных в PagedModel с HATEOAS ссылками</li>
 * </ul>
 * 
 * <p><b>Паттерны проектирования:</b>
 * <ul>
 *   <li><b>Controller Pattern</b> - обработка HTTP запросов</li>
 *   <li><b>Dependency Injection</b> - внедрение зависимостей через конструктор</li>
 *   <li><b>Adapter Pattern</b> - адаптация между HTTP и бизнес-логикой</li>
 *   <li><b>DTO Pattern</b> - использование DTO для передачи данных</li>
 * </ul>
 * 
 * <p><b>Обработка запросов:</b>
 * <ol>
 *   <li>Получение HTTP запроса</li>
 *   <li>Валидация входных данных (автоматически через @Valid)</li>
 *   <li>Вызов соответствующего метода сервиса</li>
 *   <li>Преобразование результата в HATEOAS модель</li>
 *   <li>Возврат HTTP ответа с соответствующим статус-кодом</li>
 * </ol>
 * 
 * <p><b>HATEOAS (Hypermedia as the Engine of Application State):</b>
 * Все ответы содержат ссылки на связанные ресурсы, что позволяет клиенту навигироваться
 * по API без знания точных URL. Например, ответ содержит ссылки на self, client, table, reservations.
 * 
 * <p><b>Пример работы:</b>
 * <pre>{@code
 * 1. Клиент отправляет: POST /api/reservations
 * 2. Spring MVC направляет запрос в метод createReservation()
 * 3. Контроллер вызывает reservationService.create(request)
 * 4. Сервис выполняет бизнес-логику и возвращает ReservationResponse
 * 5. Контроллер преобразует через assembler.toModel() в EntityModel
 * 6. Возвращается HTTP 201 Created с данными и HATEOAS ссылками
 * }</pre>
 * 
 * @author Restaurant System
 * @version 1.0
 * @see ReservationApi
 * @see ReservationService
 * @see ReservationModelAssembler
 */
@RestController
public class ReservationController implements ReservationApi {
    /** Сервис для выполнения бизнес-логики работы с бронированиями */
    private final ReservationService reservationService;
    
    /** Ассемблер для преобразования ReservationResponse в EntityModel с HATEOAS ссылками */
    private final ReservationModelAssembler assembler;
    
    /** Ассемблер для преобразования страничных данных в PagedModel с HATEOAS ссылками */
    private final PagedResourcesAssembler<ReservationResponse> pagedAssembler;

    /**
     * Конструктор с внедрением зависимостей (Dependency Injection).
     * Spring автоматически создает и внедряет все зависимости при создании бина.
     * 
     * @param reservationService Сервис для работы с бронированиями
     * @param assembler Ассемблер для преобразования в HATEOAS модели
     * @param pagedAssembler Ассемблер для страничных данных
     */
    public ReservationController(ReservationService reservationService, ReservationModelAssembler assembler, PagedResourcesAssembler<ReservationResponse> pagedAssembler) {
        this.reservationService = reservationService;
        this.assembler = assembler;
        this.pagedAssembler = pagedAssembler;
    }

    /**
     * Обработка GET запроса для получения бронирования по ID.
     * 
     * <p><b>Поток выполнения:</b>
     * <ol>
     *   <li>Вызов сервиса для получения данных</li>
     *   <li>Преобразование ReservationResponse в EntityModel с HATEOAS ссылками</li>
     *   <li>Возврат результата (Spring автоматически сериализует в JSON)</li>
     * </ol>
     * 
     * @param id Идентификатор бронирования
     * @return EntityModel с данными бронирования и HATEOAS ссылками
     * @throws com.example.Restaurant.exception.ResourceNotFoundException если бронирование не найдено
     */
    @Override
    public EntityModel<ReservationResponse> getReservationById(Long id) {
        return assembler.toModel(reservationService.findById(id));
    }

    /**
     * Обработка GET запроса для получения списка бронирований с фильтрацией и пагинацией.
     * 
     * <p><b>Особенности:</b>
     * <ul>
     *   <li>Поддержка множественных фильтров (clientId, tableId, status, tableType)</li>
     *   <li>Ручная пагинация (так как фильтрация выполняется в памяти)</li>
     *   <li>Преобразование PagedResponse в Spring Page, затем в PagedModel</li>
     * </ul>
     * 
     * <p><b>Поток выполнения:</b>
     * <ol>
     *   <li>Вызов сервиса с фильтрами и параметрами пагинации</li>
     *   <li>Получение PagedResponse с данными</li>
     *   <li>Создание Spring Page из данных</li>
     *   <li>Преобразование в PagedModel с HATEOAS ссылками</li>
     * </ol>
     * 
     * @param clientId Фильтр по ID клиента (опционально)
     * @param tableId Фильтр по ID столика (опционально)
     * @param status Фильтр по статусу (опционально)
     * @param tableType Фильтр по типу столика (опционально)
     * @param page Номер страницы (начинается с 0)
     * @param size Размер страницы
     * @return PagedModel с данными, метаинформацией о пагинации и HATEOAS ссылками
     */
    @Override
    public PagedModel<EntityModel<ReservationResponse>> getAllReservations(Long clientId, Long tableId, String status, String tableType, int page, int size) {
        PagedResponse<ReservationResponse> pagedResponse = reservationService.findAll(clientId, tableId, status,tableType,  page, size);
        Page<ReservationResponse> pageImpl = new PageImpl<>(pagedResponse.content(), PageRequest.of(page, size), pagedResponse.totalElements());
        return pagedAssembler.toModel(pageImpl, assembler);
    }

    /**
     * Обработка POST запроса для создания нового бронирования.
     * 
     * <p><b>Особенности:</b>
     * <ul>
     *   <li>Возвращает HTTP 201 Created (вместо 200 OK)</li>
     *   <li>В заголовке Location указывается URL созданного ресурса</li>
     *   <li>Использует HATEOAS ссылку "self" для получения URL ресурса</li>
     * </ul>
     * 
     * <p><b>Поток выполнения:</b>
     * <ol>
     *   <li>Валидация входных данных (автоматически через @Valid)</li>
     *   <li>Вызов сервиса для создания бронирования</li>
     *   <li>Преобразование в EntityModel</li>
     *   <li>Извлечение URL из HATEOAS ссылки "self"</li>
     *   <li>Возврат ResponseEntity с кодом 201 и заголовком Location</li>
     * </ol>
     * 
     * @param request Данные для создания бронирования (валидируются автоматически)
     * @return ResponseEntity с кодом 201 Created, данными и заголовком Location
     * @throws com.example.Restaurant.exception.ResourceNotFoundException если клиент или столик не найдены
     * @throws com.example.Restaurant.exception.ConflictException если столик недоступен
     */
    @Override
    public ResponseEntity<EntityModel<ReservationResponse>> createReservation(ReservationRequest request) {
        ReservationResponse created = reservationService.create(request);
        EntityModel<ReservationResponse> model = assembler.toModel(created);
        return ResponseEntity.created(model.getRequiredLink("self").toUri())
                .body(model);
    }

    /**
     * Обработка PUT запроса для обновления бронирования.
     * 
     * @param id Идентификатор бронирования для обновления
     * @param request Новые данные бронирования
     * @return EntityModel с обновленными данными
     * @throws com.example.Restaurant.exception.ResourceNotFoundException если бронирование не найдено
     * @throws com.example.Restaurant.exception.ConflictException если столик недоступен на новое время
     */
    @Override
    public EntityModel<ReservationResponse> updateReservation(Long id, UpdateReservationRequest request) {
        return assembler.toModel(reservationService.update(id, request));
    }

    /**
     * Обработка PATCH запроса для изменения статуса бронирования.
     * 
     * @param id Идентификатор бронирования
     * @param status Новый статус
     * @return EntityModel с обновленными данными
     * @throws IllegalArgumentException если переход статуса недопустим
     * @throws com.example.Restaurant.exception.ResourceNotFoundException если бронирование не найдено
     */
    @Override
    public EntityModel<ReservationResponse> updateReservationStatus(Long id, String status) {
        return assembler.toModel(reservationService.changeStatus(id, status));
    }

    /**
     * Обработка DELETE запроса для удаления бронирования.
     * 
     * <p><b>Особенности:</b>
     * <ul>
     *   <li>Возвращает void, Spring автоматически установит HTTP 204 No Content</li>
     *   <li>Идемпотентная операция</li>
     * </ul>
     * 
     * @param id Идентификатор бронирования для удаления
     * @throws com.example.Restaurant.exception.ResourceNotFoundException если бронирование не найдено
     */
    @Override
    public void deleteReservation(Long id) {
        reservationService.delete(id);
    }
}