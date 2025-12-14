package com.example.Restaurant.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ClientRequest(
        @NotBlank(message = "Имя не может быть пустым") String name,
        @Email(message = "Некорректный email") @NotBlank(message = "Email не может быть пустым") String email,
        @NotBlank(message = "Телефон не может быть пустым") String phone
) {}
