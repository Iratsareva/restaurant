package com.example.demo.listeners;

import org.example.restaurant.events.ReservationPricedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class InternalReservationPriceListener {
    private static final Logger log = LoggerFactory.getLogger(InternalReservationPriceListener.class);

    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(name = "q.demo.reservation.price.log", durable = "true"),
                    exchange = @Exchange(name = "reservation-fanout", type = "fanout")
            )
    )
    public void logPrice(ReservationPricedEvent event) {
        log.info("Internal log: Reservation {} priced at {} (verdict: {})", event.reservationId(), event.price(), event.verdict());
    }
}