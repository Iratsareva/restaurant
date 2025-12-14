package org.example.restaurant.events;


import java.io.Serializable;
import java.time.LocalDateTime;

public record ReservationCreatedEvent(
        Long reservationId,
        Long clientId,
        String clientName,
        Long tableId,
        String tableNumber,
        LocalDateTime reservationTime,
        int numberOfGuests
) implements Serializable {}