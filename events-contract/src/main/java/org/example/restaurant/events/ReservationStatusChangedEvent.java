package org.example.restaurant.events;

import java.io.Serializable;

public record ReservationStatusChangedEvent(
        Long reservationId,
        Long clientId,
        String oldStatus,
        String newStatus
) implements Serializable {}





