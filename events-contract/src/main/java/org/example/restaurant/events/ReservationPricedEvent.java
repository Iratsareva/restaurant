package org.example.restaurant.events;

import java.io.Serializable;

public record ReservationPricedEvent(
        Long reservationId,
        Long clientId,
        Long tableId,
        double price,
        String verdict
) implements Serializable {}
