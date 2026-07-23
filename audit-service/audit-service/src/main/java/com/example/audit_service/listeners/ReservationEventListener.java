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

/**
 * Слушатель событий RabbitMQ для аудита операций с бронированиями.
 * 
 * <p><b>Назначение:</b> Этот класс слушает события из RabbitMQ и логирует их
 * для целей аудита и мониторинга системы.
 * 
 * <p><b>Архитектурная роль:</b>
 * <ul>
 *   <li><b>Audit Service</b> - сервис аудита операций</li>
 *   <li><b>Event Consumer</b> - потребитель событий для логирования</li>
 *   <li><b>Statistics Tracker</b> - отслеживание статистики бронирований</li>
 * </ul>
 * 
 * <p><b>Обрабатываемые события:</b>
 * <ul>
 *   <li>ReservationCreatedEvent - логирование создания</li>
 *   <li>ReservationDeletedEvent - логирование удаления</li>
 *   <li>ReservationPricedEvent - логирование расчета цены</li>
 * </ul>
 * 
 * <p><b>Dead Letter Queue (DLQ):</b> Настроена обработка ошибок через DLQ.
 * Если обработка события не удалась, сообщение отправляется в DLQ для последующего анализа.
 * 
 * <p><b>Дедупликация:</b> Использует Set для отслеживания обработанных событий
 * и предотвращения дублирования.
 * 
 * @author Restaurant System
 * @version 1.0
 * @see org.springframework.amqp.rabbit.annotation.RabbitListener
 */
@Component
public class ReservationEventListener {
    private static final Logger log = LoggerFactory.getLogger(ReservationEventListener.class);
    private static final String EXCHANGE_NAME = "restaurant-exchange";
    private static final String QUEUE_NAME_CREATED = "audit-reservation-queue";
    private static final String QUEUE_NAME_DELETED = "audit-reservation-delete-queue";
    private final Set<Long> processedReservationCreations = ConcurrentHashMap.newKeySet();
    private final Map<Long, String> reservations = new ConcurrentHashMap<>();

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
                    key = "reservation.created"
            )
    )
    public void handleReservationCreated(@Payload ReservationCreatedEvent event,
                                         Channel channel,
                                         @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        try {
            log.info("Received ReservationCreatedEvent: {}", event);
            if (!processedReservationCreations.add(event.reservationId())) {
                log.warn("Duplicate event received for reservationId: {}", event.reservationId());
                channel.basicAck(deliveryTag, false);
                return;
            }
            if (event.clientName().equalsIgnoreCase("CRASH")) {
                throw new RuntimeException("Simulating processing error for DLQ test");
            }

            log.info("NEW RESERVATION: ID={}, Client='{}' (ID: {}), Table='{}', Time={}, Guests={}",
                    event.reservationId(), event.clientName(), event.clientId(), event.tableNumber(),
                    event.reservationTime(), event.numberOfGuests());

            reservations.put(event.reservationId(), event.clientName());
            log.info("Total reservations now: {}", reservations.size());

            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("Failed to process event: {}. Sending to DLQ.", event, e);
            channel.basicNack(deliveryTag, false, false);
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

            log.info("RESERVATION DELETED: ID={}", event.reservationId());

            if (reservations.remove(event.reservationId()) != null) {
                log.info("Removed from statistics. Total reservations now: {}", reservations.size());
            }

            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("Failed to process event: {}. Sending to DLQ.", event, e);
            channel.basicNack(deliveryTag, false, false);
        }
    }

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