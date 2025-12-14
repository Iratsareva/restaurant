package com.example.Restaurant.dto;


import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import java.time.LocalDateTime;

// DTO для обновления. Не меняем clientId или tableId.
public record UpdateReservationRequest(
        @Future(message = "Время бронирования должно быть в будущем") LocalDateTime reservationTime,
        @Min(value = 1, message = "Количество гостей не менее 1") int numberOfGuests
) {}
