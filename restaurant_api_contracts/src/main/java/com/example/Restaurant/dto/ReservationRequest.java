package com.example.Restaurant.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record ReservationRequest(
        @NotNull(message = "ID клиента обязателен") Long clientId,
        @NotNull(message = "ID столика обязателен") Long tableId,
        @Future(message = "Время бронирования обязателен") LocalDateTime reservationTime,
        @Min(value = 1, message = "Количество гостей не менее 1") int numberOfGuests) {}

