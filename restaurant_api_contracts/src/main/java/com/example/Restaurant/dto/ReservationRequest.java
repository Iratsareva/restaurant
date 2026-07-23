package com.example.Restaurant.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * DTO (Data Transfer Object) для запроса на создание бронирования.
 * 
 * <p><b>Назначение:</b> Этот класс представляет собой неизменяемый (immutable) объект передачи данных,
 * который используется для получения информации от клиента при создании нового бронирования.
 * Использует Java Record (доступно с Java 14+), что автоматически создает:
 * - Публичные final поля
 * - Конструктор со всеми параметрами
 * - Методы equals(), hashCode(), toString()
 * 
 * <p><b>Валидация:</b> Все поля имеют аннотации валидации из Jakarta Bean Validation API:
 * - @NotNull - проверяет, что поле не null
 * - @Future - проверяет, что дата в будущем
 * - @Min - проверяет минимальное значение
 * 
 * <p><b>Использование:</b> Этот объект передается в метод create() сервиса ReservationService
 * через REST API контроллер или GraphQL мутацию.
 * 
 * <p><b>Пример использования:</b>
 * <pre>{@code
 * ReservationRequest request = new ReservationRequest(
 *     1L,                    // clientId
 *     5L,                    // tableId
 *     LocalDateTime.now().plusHours(2),  // reservationTime
 *     4                      // numberOfGuests
 * );
 * }</pre>
 * 
 * @param clientId ID клиента, который делает бронирование. Должен существовать в базе данных.
 * @param tableId ID столика, который нужно забронировать. Должен существовать и быть доступным.
 * @param reservationTime Дата и время бронирования. Должно быть в будущем (валидация @Future).
 * @param numberOfGuests Количество гостей. Минимум 1 человек (валидация @Min(1)).
 * 
 * @author Restaurant System
 * @version 1.0
 * @see ReservationResponse
 * @see com.example.demo.service.ReservationService#create(ReservationRequest)
 */
public record ReservationRequest(
        @NotNull(message = "ID клиента обязателен") Long clientId,
        @NotNull(message = "ID столика обязателен") Long tableId,
        @Future(message = "Время бронирования обязательно") LocalDateTime reservationTime,
        @Min(value = 1, message = "Количество гостей не менее 1") int numberOfGuests) {}

