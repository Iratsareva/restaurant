package com.example.demo.service;



import com.example.Restaurant.dto.*;
import com.example.Restaurant.exception.ConflictException;
import com.example.Restaurant.exception.ResourceNotFoundException;

import com.example.demo.config.RabbitMQConfig;
import com.example.demo.models.Client;
import com.example.demo.models.Reservation;
import com.example.demo.models.TableEntity;
import com.example.demo.repository.ClientRepository;
import com.example.demo.repository.ReservationRepository;
import com.example.demo.repository.TableRepository;
import org.example.restaurant.events.ReservationCreatedEvent;
import org.example.restaurant.events.ReservationDeletedEvent;
import org.example.restaurant.events.ReservationStatusChangedEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.example.reservationprice.PriceRequest;
import org.example.reservationprice.PriceResponse;
import org.example.reservationprice.ReservationPriceServiceGrpc;
import io.grpc.StatusRuntimeException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.example.restaurant.events.ReservationPricedEvent;

/**
 * Сервисный класс для управления бизнес-логикой работы с бронированиями.
 * 
 * <p><b>Назначение:</b> Этот класс содержит всю бизнес-логику для работы с бронированиями столиков.
 * Является центральным компонентом системы и координирует работу между различными слоями:
 * репозиториями, другими сервисами, внешними сервисами (gRPC) и системой обмена сообщениями (RabbitMQ).
 * 
 * <p><b>Архитектурная роль:</b>
 * <ul>
 *   <li><b>Business Logic Layer</b> - слой бизнес-логики, реализует правила предметной области</li>
 *   <li><b>Transaction Management</b> - управление транзакциями через @Transactional</li>
 *   <li><b>Integration Point</b> - точка интеграции с внешними сервисами (gRPC, RabbitMQ)</li>
 *   <li><b>Orchestration</b> - оркестрация операций между различными компонентами</li>
 * </ul>
 * 
 * <p><b>Основные функции:</b>
 * <ul>
 *   <li>Создание бронирований с валидацией и проверкой конфликтов</li>
 *   <li>Расчет цены через синхронный gRPC вызов</li>
 *   <li>Публикация событий в RabbitMQ для других микросервисов</li>
 *   <li>Управление статусами бронирований (state machine)</li>
 *   <li>Автоматическая отмена просроченных бронирований</li>
 *   <li>Фильтрация и пагинация списков бронирований</li>
 * </ul>
 * 
 * <p><b>Интеграции:</b>
 * <ul>
 *   <li><b>gRPC</b> - синхронный вызов ReservationPriceService для расчета цены</li>
 *   <li><b>RabbitMQ</b> - асинхронная публикация событий (ReservationCreatedEvent, ReservationPricedEvent, 
 *       ReservationStatusChangedEvent, ReservationDeletedEvent)</li>
 *   <li><b>PostgreSQL</b> - через репозитории для хранения данных</li>
 * </ul>
 * 
 * <p><b>Паттерны проектирования:</b>
 * <ul>
 *   <li><b>Service Layer Pattern</b> - инкапсуляция бизнес-логики</li>
 *   <li><b>Transaction Script</b> - каждый метод выполняет одну бизнес-операцию</li>
 *   <li><b>Event-Driven Architecture</b> - публикация событий для слабосвязанной интеграции</li>
 *   <li><b>State Machine</b> - управление переходами статусов бронирований</li>
 * </ul>
 * 
 * <p><b>Жизненный цикл бронирования:</b>
 * <pre>
 * 1. Создание (PENDING) → проверки → расчет цены → события
 * 2. Подтверждение (CONFIRMED) → событие изменения статуса
 * 3. Оплата (PAID) → событие изменения статуса
 * 4. Отмена (CANCELLED) → событие изменения статуса
 * 5. Удаление → событие удаления
 * </pre>
 * 
 * <p><b>Обработка ошибок:</b>
 * <ul>
 *   <li>ResourceNotFoundException - ресурс не найден (404)</li>
 *   <li>ConflictException - конфликт данных (409, например, столик уже забронирован)</li>
 *   <li>IllegalArgumentException - невалидные данные или недопустимый переход статуса</li>
 * </ul>
 * 
 * @author Restaurant System
 * @version 1.0
 * @see ReservationRepository
 * @see ClientService
 * @see TableService
 */
@Service
public class ReservationService {
    private static final Logger log = LoggerFactory.getLogger(ReservationService.class);
    
    /** Репозиторий для работы с бронированиями в базе данных */
    private final ReservationRepository reservationRepository;
    
    /** Репозиторий для работы со столиками */
    private final TableRepository tableRepository;
    
    /** Репозиторий для работы с клиентами */
    private final ClientRepository clientRepository;
    
    /** Сервис для работы с клиентами */
    private final ClientService clientService;
    
    /** Сервис для работы со столиками */
    private final TableService tableService;
    
    /** RabbitMQ шаблон для публикации событий */
    private final RabbitTemplate rabbitTemplate;
    
    /**
     * gRPC клиент для синхронного вызова сервиса расчета цены.
     * 
     * <p><b>Назначение:</b> Блокирующий (blocking) stub для синхронных вызовов gRPC сервиса.
     * Используется для расчета цены бронирования сразу при создании.
     * 
     * <p><b>Конфигурация:</b> Имя "reservation-price-service" должно совпадать с конфигурацией
     * в application.properties (grpc.client.reservation-price-service.address).
     * 
     * <p><b>Особенности:</b>
     * <ul>
     *   <li>Блокирующий вызов - поток ожидает ответа</li>
     *   <li>Обработка ошибок через try-catch</li>
     *   <li>Если сервис недоступен, бронирование создается без цены (цена будет 0)</li>
     * </ul>
     */
    @GrpcClient("reservation-price-service")
    private ReservationPriceServiceGrpc.ReservationPriceServiceBlockingStub priceServiceStub;

    /**
     * Продолжительность бронирования по умолчанию.
     * 
     * <p><b>Использование:</b> Используется для расчета времени окончания бронирования
     * при проверке пересечений по времени. Все бронирования имеют фиксированную длительность 2 часа.
     * 
     * <p><b>Пример:</b> Если бронирование начинается в 19:00, оно заканчивается в 21:00.
     */
    private static final Duration RESERVATION_DURATION = Duration.ofHours(2);

    /**
     * Конструктор с внедрением зависимостей.
     * 
     * @param reservationRepository Репозиторий для работы с бронированиями
     * @param tableRepository Репозиторий для работы со столиками
     * @param clientRepository Репозиторий для работы с клиентами
     * @param clientService Сервис для работы с клиентами
     * @param tableService Сервис для работы со столиками
     * @param rabbitTemplate RabbitMQ шаблон для публикации событий
     */
    public ReservationService(ReservationRepository reservationRepository, TableRepository tableRepository, ClientRepository clientRepository, ClientService clientService, TableService tableService, RabbitTemplate rabbitTemplate) {
        this.reservationRepository = reservationRepository;
        this.tableRepository = tableRepository;
        this.clientRepository = clientRepository;
        this.clientService = clientService;
        this.tableService = tableService;
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Получить список всех бронирований с поддержкой фильтрации и пагинации.
     * 
     * <p><b>Транзакция:</b> Только для чтения (@Transactional(readOnly = true)) - оптимизация производительности.
     * 
     * <p><b>Логика фильтрации:</b>
     * <ul>
     *   <li>Если указан clientId - фильтр по клиенту (приоритет 1)</li>
     *   <li>Иначе, если указан tableId - фильтр по столику (приоритет 2)</li>
     *   <li>Иначе, если указан status - фильтр по статусу (приоритет 3)</li>
     *   <li>Иначе, если указан tableType - фильтр по типу столика (приоритет 4)</li>
     *   <li>Иначе - все бронирования</li>
     * </ul>
     * 
     * <p><b>Пагинация:</b> Выполняется в памяти после получения всех данных.
     * Для больших объемов данных рекомендуется использовать SQL пагинацию.
     * 
     * <p><b>Производительность:</b> Метод загружает все данные в память, что может быть проблемой
     * при большом количестве бронирований. Для продакшена рекомендуется оптимизация через SQL.
     * 
     * @param clientId Фильтр по ID клиента (опционально)
     * @param tableId Фильтр по ID столика (опционально)
     * @param status Фильтр по статусу (опционально)
     * @param tableType Фильтр по типу столика (опционально)
     * @param page Номер страницы (начинается с 0)
     * @param size Размер страницы
     * @return PagedResponse с данными, метаинформацией о пагинации
     */
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public PagedResponse<ReservationResponse> findAll(Long clientId, Long tableId, String status,String tableType, int page, int size) {
        List<Reservation> reservations;
        if (clientId != null) {
            reservations = reservationRepository.findByClientId(clientId);
        } else if (tableId != null) {
            reservations = reservationRepository.findByTableId(tableId);
        } else if (status != null) {
            reservations = reservationRepository.findByStatus(status);
        }else if (tableType != null) {
            reservations = reservationRepository.findByTableType(tableType);
        } else {
            reservations = reservationRepository.findAll();
        }

        int totalElements = reservations.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);
        int from = page * size;
        int to = Math.min(from + size, totalElements);
        
        List<Reservation> pageContent = (from >= totalElements || from < 0)
                ? List.of() 
                : reservations.subList(from, to);

        List<ReservationResponse> content = pageContent.stream().map(this::toResponse).collect(Collectors.toList());
        boolean last = page >= totalPages - 1;

        return new PagedResponse<>(content, page, size, totalElements, totalPages, last);
    }

    /**
     * Найти бронирование по идентификатору.
     * 
     * <p><b>Описание:</b> Получает бронирование из базы данных и преобразует в DTO.
     * 
     * @param id Идентификатор бронирования
     * @return ReservationResponse с данными бронирования
     * @throws ResourceNotFoundException если бронирование не найдено
     */
    public ReservationResponse findById(Long id) {
        Reservation reservation = reservationRepository.findById(id);
        if (reservation == null) {
            throw new ResourceNotFoundException("Reservation", id);
        }
        return toResponse(reservation);
    }

    /**
     * Создать новое бронирование.
     * 
     * <p><b>Описание:</b> Это самый сложный метод сервиса, который выполняет множество проверок
     * и интеграций. Реализует полный жизненный цикл создания бронирования.
     * 
     * <p><b>Последовательность выполнения:</b>
     * <ol>
     *   <li><b>Валидация существования ресурсов:</b> Проверка существования клиента и столика</li>
     *   <li><b>Проверка доступности столика:</b> Столик должен быть доступен (isAvailable = true)</li>
     *   <li><b>Проверка вместимости:</b> Количество гостей не должно превышать количество мест</li>
     *   <li><b>Проверка пересечений по времени:</b> Столик не должен быть забронирован на это время</li>
     *   <li><b>Создание бронирования:</b> Сохранение в базе данных со статусом PENDING</li>
     *   <li><b>Расчет цены:</b> Синхронный вызов gRPC сервиса для расчета цены</li>
     *   <li><b>Публикация событий:</b> Отправка событий в RabbitMQ для других микросервисов</li>
     * </ol>
     * 
     * <p><b>Проверка пересечений по времени:</b>
     * Бронирование длится 2 часа (RESERVATION_DURATION). Проверяется, что нет других
     * активных (не отмененных) бронирований, которые пересекаются по времени.
     * 
     * <p><b>Расчет цены через gRPC:</b>
     * <ul>
     *   <li>Синхронный блокирующий вызов gRPC сервиса</li>
     *   <li>Передаются: reservationId, numberOfGuests, tableType, durationHours (2)</li>
     *   <li>Получаются: price (цена) и verdict (AFFORDABLE/EXPENSIVE)</li>
     *   <li>Если сервис недоступен - бронирование создается с ценой 0 (graceful degradation)</li>
     * </ul>
     * 
     * <p><b>События в RabbitMQ:</b>
     * <ul>
     *   <li><b>ReservationCreatedEvent</b> - публикуется сразу после создания</li>
     *   <li><b>ReservationPricedEvent</b> - публикуется после расчета цены (в Topic и Fanout exchange)</li>
     * </ul>
     * 
     * <p><b>Транзакция:</b> Метод выполняется в транзакции (по умолчанию @Transactional).
     * Если произойдет ошибка, все изменения откатятся.
     * 
     * @param request Данные для создания бронирования (уже валидированы на уровне контроллера)
     * @return ReservationResponse с данными созданного бронирования
     * @throws ResourceNotFoundException если клиент или столик не найдены
     * @throws ConflictException если столик недоступен или уже забронирован
     * @throws IllegalArgumentException если количество гостей превышает вместимость столика
     */
    public ReservationResponse create(ReservationRequest request) {
        Client clientEntity = clientRepository.findById(request.clientId());
        if (clientEntity == null) {
            throw new ResourceNotFoundException("Client", request.clientId());
        }

        TableEntity tableEntity = tableRepository.findById(request.tableId());
        if (tableEntity == null) {
            throw new ResourceNotFoundException("Table", request.tableId());
        }

        if (!tableEntity.isAvailable()) {
            throw new ConflictException("Стол недоступен для бронирования");
        }
        if (request.numberOfGuests() > tableEntity.getNumberOfSeats()) {
            throw new IllegalArgumentException("Количество гостей превышает количество мест за столом");
        }

        LocalDateTime endTime = request.reservationTime().plus(RESERVATION_DURATION);
        List<Reservation> overlapping = reservationRepository.findOverlapping(request.tableId(), request.reservationTime(), endTime);
        if (!overlapping.isEmpty()) {
            throw new ConflictException("Стол уже забронирован на указанное время");
        }

        Reservation reservation = new Reservation();
        reservation.setClient(clientEntity);
        reservation.setTable(tableEntity);
        reservation.setReservationTime(request.reservationTime());
        reservation.setNumberOfGuests(request.numberOfGuests());
        reservation.setStatus("PENDING");
        reservation.setCreatedAt(LocalDateTime.now());

        reservation = reservationRepository.create(reservation);

        ReservationCreatedEvent event = new ReservationCreatedEvent(
                reservation.getId(),
                clientEntity.getId(),
                clientEntity.getName(),
                tableEntity.getId(),
                tableEntity.getNumber(),
                tableEntity.getType() != null ? tableEntity.getType() : "STANDARD",
                reservation.getReservationTime(),
                reservation.getNumberOfGuests()
        );

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                RabbitMQConfig.ROUTING_KEY_RESERVATION_CREATED,
                event
        );

        log.info("Reservation created event published: reservationId={}", reservation.getId());

        // ========== РАСЧЕТ ЦЕНЫ ЧЕРЕЗ gRPC ==========
        // Синхронный вызов внешнего сервиса для расчета цены бронирования
        // Используется блокирующий stub, поэтому вызов ожидает ответа
        try {
            if (priceServiceStub != null) {
                // Определение типа столика (по умолчанию STANDARD)
                String tableType = tableEntity.getType() != null && !tableEntity.getType().isBlank() 
                        ? tableEntity.getType() 
                        : "STANDARD";
                
                // Создание gRPC запроса с использованием Builder паттерна
                PriceRequest priceRequest = PriceRequest.newBuilder()
                        .setReservationId(reservation.getId())
                        .setNumberOfGuests(reservation.getNumberOfGuests())
                        .setTableType(tableType)
                        .setDurationHours(2)  // Фиксированная длительность 2 часа
                        .build();

                // Синхронный блокирующий вызов gRPC сервиса
                // Поток блокируется до получения ответа
                PriceResponse priceResponse = priceServiceStub.calculatePrice(priceRequest);
                
                // Обновление бронирования с рассчитанной ценой и вердиктом
                reservation.setPrice(priceResponse.getPrice());
                reservation.setVerdict(priceResponse.getVerdict());  // EXPENSIVE или AFFORDABLE
                reservation = reservationRepository.update(reservation);
                
                log.info("Price calculated synchronously for reservationId={}, price={}, verdict={}", 
                        reservation.getId(), priceResponse.getPrice(), priceResponse.getVerdict());

                // Создание события о расчете цены
                ReservationPricedEvent pricedEvent = new ReservationPricedEvent(
                        reservation.getId(),
                        clientEntity.getId(),
                        tableEntity.getId(),
                        priceResponse.getPrice(),
                        priceResponse.getVerdict()
                );
                
                // Публикация события в Topic Exchange (для конкретных подписчиков)
                rabbitTemplate.convertAndSend(
                        RabbitMQConfig.EXCHANGE_NAME,
                        RabbitMQConfig.ROUTING_KEY_RESERVATION_PRICED,
                        pricedEvent
                );
                
                // Публикация события в Fanout Exchange (для всех подписчиков)
                // Fanout exchange отправляет сообщение во все связанные очереди
                rabbitTemplate.convertAndSend(RabbitMQConfig.FANOUT_EXCHANGE, "", pricedEvent);
                
                log.info("ReservationPricedEvent published: reservationId={}, price={}", 
                        reservation.getId(), priceResponse.getPrice());
            } else {
                // Graceful degradation: если gRPC сервис недоступен, бронирование создается без цены
                log.warn("gRPC price service stub is not available, price will be calculated asynchronously");
            }
        } catch (StatusRuntimeException e) {
            // Обработка ошибок gRPC (например, сервис недоступен)
            // Бронирование все равно создается, но без цены (graceful degradation)
            log.error("Error calculating price synchronously for reservationId={}: {}", 
                    reservation.getId(), e.getStatus(), e);
        } catch (Exception e) {
            // Обработка любых других неожиданных ошибок
            log.error("Unexpected error calculating price synchronously for reservationId={}: {}", 
                    reservation.getId(), e.getMessage(), e);
        }
        return toResponse(reservation);
    }

    /**
     * Обновить существующее бронирование.
     * 
     * <p><b>Описание:</b> Обновляет время и количество гостей бронирования.
     * Выполняет те же проверки, что и при создании (вместимость, пересечения по времени).
     * 
     * <p><b>Проверки:</b>
     * <ul>
     *   <li>Существование бронирования</li>
     *   <li>Количество гостей не превышает вместимость столика</li>
     *   <li>Отсутствие пересечений по времени (исключая само обновляемое бронирование)</li>
     * </ul>
     * 
     * <p><b>Примечание:</b> Статус бронирования обновляется отдельным методом changeStatus().
     * 
     * @param id Идентификатор бронирования для обновления
     * @param request Новые данные (время и количество гостей)
     * @return ReservationResponse с обновленными данными
     * @throws ResourceNotFoundException если бронирование не найдено
     * @throws ConflictException если столик уже забронирован на новое время
     * @throws IllegalArgumentException если количество гостей превышает вместимость
     */
    public ReservationResponse update(Long id, UpdateReservationRequest request) {
        Reservation reservation = reservationRepository.findById(id);
        if (reservation == null) {
            throw new ResourceNotFoundException("Reservation", id);
        }

        TableEntity tableEntity = reservation.getTable();

        if (request.numberOfGuests() > tableEntity.getNumberOfSeats()) {
            throw new IllegalArgumentException("Количество гостей превышает количество мест за столом");
        }

        LocalDateTime endTime = request.reservationTime().plus(RESERVATION_DURATION);
        List<Reservation> overlapping = reservationRepository.findOverlapping(tableEntity.getId(), request.reservationTime(), endTime);
        overlapping.removeIf(r -> r.getId().equals(id));
        if (!overlapping.isEmpty()) {
            throw new ConflictException("Стол уже забронирован на указанное время");
        }

        reservation.setReservationTime(request.reservationTime());
        reservation.setNumberOfGuests(request.numberOfGuests());
        reservation = reservationRepository.update(reservation);
        return toResponse(reservation);
    }


    /**
     * Изменить статус бронирования.
     * 
     * <p><b>Описание:</b> Реализует модель конечного автомата (state machine) для управления статусами.
     * Поддерживает строгие правила переходов между статусами для обеспечения целостности данных.
     * 
     * <p><b>Модель переходов статусов (State Machine):</b>
     * <pre>
     * PENDING (начальное состояние)
     *   ├─→ CONFIRMED (подтверждено)
     *   ├─→ PAID (оплачено)
     *   └─→ CANCELLED (отменено)
     * 
     * CONFIRMED
     *   ├─→ PAID (оплачено)
     *   └─→ CANCELLED (отменено)
     * 
     * PAID
     *   └─→ CANCELLED (отменено)
     * 
     * CANCELLED (финальное состояние, нет переходов)
     * </pre>
     * 
     * <p><b>Проверки:</b>
     * <ul>
     *   <li>Существование бронирования</li>
     *   <li>Валидность перехода статуса (через isValidStatusTransition)</li>
     *   <li>Попытка установить тот же статус игнорируется (идемпотентность)</li>
     * </ul>
     * 
     * <p><b>События:</b> После успешного изменения статуса публикуется событие
     * ReservationStatusChangedEvent в RabbitMQ, которое получают:
     * <ul>
     *   <li>Audit Service - для логирования</li>
     *   <li>Notification Service - для отправки уведомлений клиенту через WebSocket</li>
     * </ul>
     * 
     * @param id Идентификатор бронирования
     * @param newStatus Новый статус (CONFIRMED, PAID, CANCELLED)
     * @return ReservationResponse с обновленным статусом
     * @throws ResourceNotFoundException если бронирование не найдено
     * @throws IllegalArgumentException если переход статуса недопустим
     */
    public ReservationResponse changeStatus(Long id, String newStatus) {
        Reservation reservation = reservationRepository.findById(id);
        if (reservation == null) {
            throw new ResourceNotFoundException("Reservation", id);
        }
        String currentStatus = reservation.getStatus();
        if (!isValidStatusTransition(currentStatus, newStatus)) {
            throw new IllegalArgumentException(
                String.format("Недопустимый переход статуса: %s -> %s. " +
                    "Допустимые переходы: PENDING -> CONFIRMED/PAID/CANCELLED, CONFIRMED -> PAID/CANCELLED, PAID -> CANCELLED",
                    currentStatus, newStatus)
            );
        }
        if (currentStatus.equals(newStatus)) {
            log.warn("Попытка изменить статус на тот же самый: reservationId={}, status={}", id, newStatus);
            return toResponse(reservation);
        }
        reservation.setStatus(newStatus);
        reservation = reservationRepository.update(reservation);
        
        log.info("Статус бронирования изменен: reservationId={}, {} -> {}", id, currentStatus, newStatus);
        ReservationStatusChangedEvent statusEvent = new ReservationStatusChangedEvent(
                reservation.getId(),
                reservation.getClient().getId(),
                currentStatus,
                newStatus
        );
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                RabbitMQConfig.ROUTING_KEY_RESERVATION_STATUS_CHANGED,
                statusEvent
        );
        log.info("Reservation status changed event published: reservationId={}, {} -> {}", 
                reservation.getId(), currentStatus, newStatus);
        
        return toResponse(reservation);
    }


    /**
     * Проверить, допустим ли переход между статусами.
     * 
     * <p><b>Описание:</b> Реализует правила переходов статусов согласно модели конечного автомата.
     * 
     * <p><b>Правила переходов:</b>
     * <ul>
     *   <li>PENDING → CONFIRMED, PAID, CANCELLED (разрешено)</li>
     *   <li>CONFIRMED → PAID, CANCELLED (разрешено)</li>
     *   <li>PAID → CANCELLED (разрешено)</li>
     *   <li>CANCELLED → любой статус (запрещено, финальное состояние)</li>
     *   <li>Любой другой переход (запрещено)</li>
     * </ul>
     * 
     * <p><b>Особенности:</b>
     * <ul>
     *   <li>Сравнение без учета регистра (toUpperCase)</li>
     *   <li>Обработка null значений</li>
     * </ul>
     * 
     * @param currentStatus Текущий статус бронирования
     * @param newStatus Новый статус, на который нужно перейти
     * @return true если переход допустим, false если запрещен
     */
    private boolean isValidStatusTransition(String currentStatus, String newStatus) {
        String current = currentStatus != null ? currentStatus.toUpperCase() : "";
        String next = newStatus != null ? newStatus.toUpperCase() : "";
        if ("PENDING".equals(current)) {
            return "CONFIRMED".equals(next) || "PAID".equals(next) || "CANCELLED".equals(next);
        }
        if ("CONFIRMED".equals(current)) {
            return "PAID".equals(next) || "CANCELLED".equals(next);
        }
        if ("PAID".equals(current)) {
            return "CANCELLED".equals(next);
        }
        if ("CANCELLED".equals(current)) {
            return false;
        }
        return true;
    }

    /**
     * Автоматически отменить просроченные PENDING бронирования.
     * 
     * <p><b>Описание:</b> Находит все бронирования со статусом PENDING, которые были созданы
     * более 24 часов назад, и автоматически отменяет их (устанавливает статус CANCELLED).
     * 
     * <p><b>Логика:</b>
     * <ul>
     *   <li>Находятся все бронирования со статусом PENDING</li>
     *   <li>Для каждого проверяется время создания (createdAt)</li>
     *   <li>Если createdAt старше 24 часов - бронирование отменяется</li>
     *   <li>Если createdAt = null, используется reservationTime минус 2 часа</li>
     * </ul>
     * 
     * <p><b>Использование:</b> Вызывается планировщиком ReservationStatusScheduler
     * каждые 6 часов (cron: "0 0 */6 * * *").
     * 
     * <p><b>Обработка ошибок:</b> Если при отмене одного бронирования произошла ошибка,
     * обработка продолжается для остальных (не прерывается).
     * 
     * <p><b>События:</b> При отмене каждого бронирования публикуется событие
     * ReservationStatusChangedEvent через метод changeStatus().
     * 
     * @return Количество отмененных бронирований
     */
    public int cancelExpiredPendingReservations() {
        List<Reservation> pendingReservations = reservationRepository.findByStatus("PENDING");
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expirationTime = now.minusHours(24);
        int cancelledCount = 0;

        for (Reservation reservation : pendingReservations) {
            LocalDateTime createdAt = reservation.getCreatedAt();
            if (createdAt == null) {
                createdAt = reservation.getReservationTime().minus(RESERVATION_DURATION);
            }
            
            if (createdAt.isBefore(expirationTime)) {
                try {
                    changeStatus(reservation.getId(), "CANCELLED");
                    cancelledCount++;
                    log.info("Автоматически отменено просроченное бронирование: reservationId={}, createdAt={}", 
                            reservation.getId(), createdAt);
                } catch (Exception e) {
                    log.error("Ошибка при автоматической отмене бронирования: reservationId={}", 
                            reservation.getId(), e);
                }
            }
        }

        if (cancelledCount > 0) {
            log.info("Автоматически отменено {} просроченных бронирований", cancelledCount);
        }
        return cancelledCount;
    }

    /**
     * Удалить бронирование из базы данных.
     * 
     * <p><b>Описание:</b> Полностью удаляет бронирование из базы данных.
     * После удаления восстановление данных невозможно.
     * 
     * <p><b>Проверки:</b>
     * <ul>
     *   <li>Существование бронирования</li>
     * </ul>
     * 
     * <p><b>События:</b> После удаления публикуется событие ReservationDeletedEvent
     * в RabbitMQ, которое получают:
     * <ul>
     *   <li>Audit Service - для логирования удаления</li>
     *   <li>Notification Service - для отправки уведомления через WebSocket</li>
     * </ul>
     * 
     * <p><b>Примечание:</b> Для отмены бронирования рекомендуется использовать
     * changeStatus(id, "CANCELLED") вместо удаления, чтобы сохранить историю.
     * 
     * @param id Идентификатор бронирования для удаления
     * @throws ResourceNotFoundException если бронирование не найдено
     */
    public void delete(Long id) {
        Reservation reservation = reservationRepository.findById(id);
        if (reservation == null) {
            throw new ResourceNotFoundException("Reservation", id);
        }
        Long reservationId = reservation.getId();
        reservationRepository.delete(id);
        ReservationDeletedEvent event = new ReservationDeletedEvent(reservationId);
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                RabbitMQConfig.ROUTING_KEY_RESERVATION_DELETED,
                event
        );
    }

    /**
     * Преобразовать сущность Reservation в DTO ReservationResponse.
     * 
     * <p><b>Описание:</b> Вспомогательный метод для преобразования JPA сущности в DTO.
     * Создает вложенные объекты ClientResponse и TableResponse.
     * 
     * <p><b>Особенности:</b>
     * <ul>
     *   <li>Обработка null значений для типа столика (по умолчанию "STANDARD")</li>
     *   <li>Создание DTO объектов для клиента и столика</li>
     * </ul>
     * 
     * <p><b>Использование:</b> Вызывается во всех методах, которые возвращают ReservationResponse.
     * 
     * @param reservation JPA сущность Reservation из базы данных
     * @return ReservationResponse DTO для отправки клиенту
     */
    private ReservationResponse toResponse(Reservation reservation) {
        Client clientEntity = reservation.getClient();
        TableEntity tableEntity = reservation.getTable();
        ClientResponse client = new ClientResponse(clientEntity.getId(), clientEntity.getName(), clientEntity.getEmail(), clientEntity.getPhone());
        String tableType = tableEntity.getType() != null && !tableEntity.getType().isBlank()
                ? tableEntity.getType() 
                : "STANDARD";
        TableResponse table = new TableResponse(tableEntity.getId(), tableEntity.getNumber(), tableEntity.getNumberOfSeats(), tableType, tableEntity.isAvailable());
        return new ReservationResponse(reservation.getId(), client, table, reservation.getReservationTime(), reservation.getNumberOfGuests(), reservation.getStatus(), reservation.getPrice());
    }
}