package org.example.restaurant.events;

import java.io.Serializable;

public record ReservationDeletedEvent(
        Long reservationId
) implements Serializable {}
