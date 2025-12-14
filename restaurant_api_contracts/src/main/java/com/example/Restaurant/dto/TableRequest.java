package com.example.Restaurant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;


public record TableRequest(
        @NotBlank(message = "Номер столика не может быть пустым") String number,
        @Positive(message = "Количество мест должно быть не менее 1") int numberOfSeats,
        String type
) {}
