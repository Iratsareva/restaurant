package com.example.notification_service.listener;


import com.example.notification_service.websocket.NotificationHandler;
import org.example.restaurant.events.ReservationCreatedEvent;
import org.example.restaurant.events.ReservationDeletedEvent;
import org.example.restaurant.events.ReservationPricedEvent;
import org.example.restaurant.events.ReservationStatusChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

/**
 * Слушатель событий RabbitMQ для отправки уведомлений через WebSocket.
 * 
 * <p><b>Назначение:</b> Этот класс слушает события из RabbitMQ и отправляет
 * уведомления всем подключенным WebSocket клиентам в реальном времени.
 * 
 * <p><b>Архитектурная роль:</b>
 * <ul>
 *   <li><b>Event Consumer</b> - потребитель событий из RabbitMQ</li>
 *   <li><b>Adapter</b> - адаптер между RabbitMQ и WebSocket</li>
 *   <li><b>Message Transformer</b> - преобразование событий в JSON уведомления</li>
 * </ul>
 * 
 * <p><b>Поток данных:</b>
 * <pre>
 * ReservationService → RabbitMQ → NotificationListener → NotificationHandler → WebSocket клиенты
 * </pre>
 * 
 * <p><b>Обрабатываемые события:</b>
 * <ul>
 *   <li>ReservationCreatedEvent - создание бронирования</li>
 *   <li>ReservationPricedEvent - расчет цены</li>
 *   <li>ReservationDeletedEvent - удаление бронирования</li>
 *   <li>ReservationStatusChangedEvent - изменение статуса</li>
 * </ul>
 * 
 * <p><b>Формат уведомлений:</b> JSON объекты с полем "type" и данными события.
 * 
 * @author Restaurant System
 * @version 1.0
 * @see NotificationHandler
 * @see org.springframework.amqp.rabbit.annotation.RabbitListener
 */
@Component
public class NotificationListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationListener.class);
    private final NotificationHandler notificationHandler;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public NotificationListener(NotificationHandler notificationHandler) {
        this.notificationHandler = notificationHandler;
    }

    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(name = "q.notifications.reservation.created", durable = "true"),
                    exchange = @Exchange(name = "restaurant-exchange", type = "topic"),
                    key = "reservation.created"
            )
    )
    public void handleReservationCreated(ReservationCreatedEvent event) {
        log.info("Получено событие создания бронирования: {}", event);

        try {
            Map<String, Object> message = Map.of(
                    "type", "RESERVATION_CREATED",
                    "reservationId", event.reservationId(),
                    "clientId", event.clientId(),
                    "clientName", event.clientName(),
                    "tableId", event.tableId(),
                    "tableNumber", event.tableNumber(),
                    "reservationTime", event.reservationTime().toString(),
                    "numberOfGuests", event.numberOfGuests()
            );

            String json = objectMapper.writeValueAsString(message);
            notificationHandler.broadcast(json);
        } catch (Exception e) {
            log.error("Ошибка обработки ReservationCreatedEvent", e);
        }
    }

    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(name = "q.notifications.reservation.priced", durable = "true"),
                    exchange = @Exchange(name = "reservation-fanout", type = "fanout")
            )
    )
    public void handleReservationPriced(ReservationPricedEvent event) {
        log.info("Получено событие расчета цены: {}", event);

        try {
            Map<String, Object> message = Map.of(
                    "type", "RESERVATION_PRICED",
                    "reservationId", event.reservationId(),
                    "clientId", event.clientId(),
                    "tableId", event.tableId(),
                    "price", event.price(),
                    "verdict", event.verdict()
            );

            String json = objectMapper.writeValueAsString(message);
            notificationHandler.broadcast(json);
        } catch (Exception e) {
            log.error("Ошибка обработки ReservationPricedEvent", e);
        }
    }


    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(name = "q.notifications.reservation.deleted", durable = "true"),
                    exchange = @Exchange(name = "restaurant-exchange", type = "topic"),
                    key = "reservation.deleted"
            )
    )
    public void handleReservationDeleted(ReservationDeletedEvent event) {
        log.info("Получено событие удаления бронирования: {}", event);

        try {
            Map<String, Object> message = Map.of(
                    "type", "RESERVATION_DELETED",
                    "reservationId", event.reservationId()
            );

            String json = objectMapper.writeValueAsString(message);
            notificationHandler.broadcast(json);
        } catch (Exception e) {
            log.error("Ошибка обработки ReservationDeletedEvent", e);
        }
    }


    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(name = "q.notifications.reservation.status.changed", durable = "true"),
                    exchange = @Exchange(name = "restaurant-exchange", type = "topic"),
                    key = "reservation.status.changed"
            )
    )
    public void handleReservationStatusChanged(ReservationStatusChangedEvent event) {
        log.info("Получено событие изменения статуса бронирования: {}", event);

        try {
            Map<String, Object> message = Map.of(
                    "type", "RESERVATION_STATUS_CHANGED",
                    "reservationId", event.reservationId(),
                    "clientId", event.clientId(),
                    "oldStatus", event.oldStatus(),
                    "newStatus", event.newStatus()
            );

            String json = objectMapper.writeValueAsString(message);
            notificationHandler.broadcast(json);
        } catch (Exception e) {
            log.error("Ошибка обработки ReservationStatusChangedEvent", e);
        }
    }
}
