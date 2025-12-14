package com.example.audit_service.listeners;


import org.example.restaurant.events.ReservationCreatedEvent;
import org.example.restaurant.events.ReservationDeletedEvent;
import org.example.restaurant.events.ReservationPricedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@Component
public class ReservationEventListener {

    private static final Logger log = LoggerFactory.getLogger(ReservationEventListener.class);
    private static final String EXCHANGE_NAME = "restaurant-exchange";
    private static final String QUEUE_NAME_CREATED = "audit-reservation-queue";
    private static final String QUEUE_NAME_DELETED = "audit-reservation-delete-queue";

    private final Set<Long> processedReservationCreations = ConcurrentHashMap.newKeySet();  // Защита от дубликатов
    private final Map<Long, String> reservations = new ConcurrentHashMap<>();  // Для статистики: ID -> clientName

    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(
                            name = QUEUE_NAME_CREATED,
                            durable = "true",
                            arguments = {
                                    @Argument(name = "x-dead-letter-exchange", value = "dlx-exchange"),
                                    @Argument(name = "x-dead-letter-routing-key", value = "dlq.audit")
                            }
                    ),
                    exchange = @Exchange(name = EXCHANGE_NAME, type = "topic", durable = "true"),
                    key = "reservation.created"  // Только для created, как в задании для статистики
            )
    )
    public void handleReservationCreated(@Payload ReservationCreatedEvent event,
                                         Channel channel,
                                         @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        try {
            log.info("Received ReservationCreatedEvent: {}", event);

            // Проверка дубликата
            if (!processedReservationCreations.add(event.reservationId())) {
                log.warn("Duplicate event received for reservationId: {}", event.reservationId());
                channel.basicAck(deliveryTag, false);
                return;
            }

            // Симулируем ошибку для DLQ (опционально, для теста)
            if (event.clientName().equalsIgnoreCase("CRASH")) {
                throw new RuntimeException("Simulating processing error for DLQ test");
            }

            // Основная логика аудита
            log.info("✅ NEW RESERVATION: ID={}, Client='{}' (ID: {}), Table='{}', Time={}, Guests={}",
                    event.reservationId(), event.clientName(), event.clientId(), event.tableNumber(),
                    event.reservationTime(), event.numberOfGuests());

            // Логика статистики (интегрирована вместо отдельного сервиса)
            reservations.put(event.reservationId(), event.clientName());
            log.info("Total reservations now: {}", reservations.size());

            // Ack
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("Failed to process event: {}. Sending to DLQ.", event, e);
            channel.basicNack(deliveryTag, false, false);  // No requeue
        }
    }

    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(
                            name = QUEUE_NAME_DELETED,
                            durable = "true",
                            arguments = {
                                    @Argument(name = "x-dead-letter-exchange", value = "dlx-exchange"),
                                    @Argument(name = "x-dead-letter-routing-key", value = "dlq.audit.delete")
                            }
                    ),
                    exchange = @Exchange(name = EXCHANGE_NAME, type = "topic", durable = "true"),
                    key = "reservation.deleted"
            )
    )
    public void handleReservationDeleted(@Payload ReservationDeletedEvent event,
                                         Channel channel,
                                         @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        try {
            log.info("Received ReservationDeletedEvent: {}", event);

            // Логика аудита
            log.info("🗑️ RESERVATION DELETED: ID={}", event.reservationId());

            // Обновление статистики: Удаляем из Map (если нужно, чтобы total отражал актуальное)
            if (reservations.remove(event.reservationId()) != null) {
                log.info("Removed from statistics. Total reservations now: {}", reservations.size());
            }

            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("Failed to process event: {}. Sending to DLQ.", event, e);
            channel.basicNack(deliveryTag, false, false);
        }
    }

    // DLQ слушатель (опционально, для отладки)
    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(name = "audit-reservation-queue.dlq", durable = "true"),
                    exchange = @Exchange(name = "dlx-exchange", type = "topic", durable = "true"),
                    key = "dlq.audit"
            )
    )
    public void handleDlqMessages(Object failedMessage) {
        log.error("!!! Received message in DLQ: {}", failedMessage);
    }



    // Добавьте
    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(name = "q.audit.reservation.price", durable = "true"),
                    exchange = @Exchange(name = "reservation-fanout", type = "fanout")
            )
    )
    public void handlePrice(ReservationPricedEvent event) {
        log.info("AUDIT: Reservation {} for client {} priced at {} (verdict: {})",
                event.reservationId(), event.clientId(), event.price(), event.verdict());
    }

}
