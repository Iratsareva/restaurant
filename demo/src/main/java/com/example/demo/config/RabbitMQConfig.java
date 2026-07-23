package com.example.demo.config;

import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;


/**
 * Конфигурация RabbitMQ для системы обмена сообщениями.
 * 
 * <p><b>Назначение:</b> Этот класс настраивает RabbitMQ для асинхронной коммуникации
 * между микросервисами через паттерн Event-Driven Architecture (EDA).
 * 
 * <p><b>Архитектурная роль:</b>
 * <ul>
 *   <li><b>Message Broker Configuration</b> - настройка брокера сообщений</li>
 *   <li><b>Event Infrastructure</b> - инфраструктура для событийной архитектуры</li>
 *   <li><b>Integration Configuration</b> - конфигурация интеграции между сервисами</li>
 * </ul>
 * 
 * <p><b>RabbitMQ Exchange типы:</b>
 * <ul>
 *   <li><b>Topic Exchange</b> - маршрутизация по паттернам routing key (используется для основной коммуникации)</li>
 *   <li><b>Fanout Exchange</b> - отправка сообщений во все связанные очереди (используется для широковещательных событий)</li>
 * </ul>
 * 
 * <p><b>События, публикуемые в систему:</b>
 * <ul>
 *   <li><b>reservation.created</b> - бронирование создано (Topic Exchange)</li>
 *   <li><b>reservation.deleted</b> - бронирование удалено (Topic Exchange)</li>
 *   <li><b>reservation.priced</b> - цена рассчитана (Topic + Fanout Exchange)</li>
 *   <li><b>reservation.status.changed</b> - статус изменен (Topic Exchange)</li>
 * </ul>
 * 
 * <p><b>Подписчики на события:</b>
 * <ul>
 *   <li><b>Audit Service</b> - логирует все события для аудита</li>
 *   <li><b>Notification Service</b> - отправляет уведомления клиентам через WebSocket</li>
 * </ul>
 * 
 * <p><b>Message Converter:</b> Используется Jackson2JsonMessageConverter для автоматической
 * сериализации/десериализации Java объектов в/из JSON.
 * 
 * <p><b>Publisher Confirms:</b> Настроен confirm callback для подтверждения доставки сообщений.
 * Если сообщение не доставлено (NACK), выводится предупреждение.
 * 
 * <p><b>Durable Exchanges:</b> Все exchange помечены как durable (true), что означает,
 * что они сохраняются при перезапуске RabbitMQ.
 * 
 * @author Restaurant System
 * @version 1.0
 * @see org.springframework.amqp.rabbit.core.RabbitTemplate
 * @see org.springframework.amqp.core.TopicExchange
 * @see org.springframework.amqp.core.FanoutExchange
 */
@Configuration
public class RabbitMQConfig {
    /** Имя Topic Exchange для основной коммуникации между сервисами */
    public static final String EXCHANGE_NAME = "restaurant-exchange";
    
    /** Routing key для события создания бронирования */
    public static final String ROUTING_KEY_RESERVATION_CREATED = "reservation.created";
    
    /** Routing key для события удаления бронирования */
    public static final String ROUTING_KEY_RESERVATION_DELETED = "reservation.deleted";
    
    /** Routing key для события расчета цены бронирования */
    public static final String ROUTING_KEY_RESERVATION_PRICED = "reservation.priced";
    
    /** Routing key для события изменения статуса бронирования */
    public static final String ROUTING_KEY_RESERVATION_STATUS_CHANGED = "reservation.status.changed";
    
    /** Имя Fanout Exchange для широковещательных событий (все подписчики получают сообщение) */
    public static final String FANOUT_EXCHANGE = "reservation-fanout";


    /**
     * Создает Topic Exchange для маршрутизации сообщений по routing key.
     * 
     * <p><b>Параметры конструктора:</b>
     * <ul>
     *   <li>name - имя exchange</li>
     *   <li>durable (true) - exchange сохраняется при перезапуске RabbitMQ</li>
     *   <li>autoDelete (false) - exchange не удаляется автоматически при отсутствии очередей</li>
     * </ul>
     * 
     * <p><b>Topic Exchange:</b> Маршрутизирует сообщения в очереди на основе паттернов routing key.
     * Например, "reservation.created" попадет в очередь, привязанную к ключу "reservation.*".
     * 
     * @return Настроенный Topic Exchange
     */
    @Bean
    public TopicExchange restaurantExchange() {
        return new TopicExchange(EXCHANGE_NAME, true, false);
    }

    /**
     * Создает конвертер сообщений для сериализации/десериализации Java объектов в/из JSON.
     * 
     * <p><b>Jackson2JsonMessageConverter:</b> Автоматически преобразует:
     * <ul>
     *   <li>Java объекты → JSON при отправке сообщений</li>
     *   <li>JSON → Java объекты при получении сообщений</li>
     * </ul>
     * 
     * @return JSON конвертер сообщений
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * Создает RabbitTemplate для отправки сообщений в RabbitMQ.
     * 
     * <p><b>RabbitTemplate:</b> Основной класс для работы с RabbitMQ в Spring.
     * Предоставляет методы для отправки и получения сообщений.
     * 
     * <p><b>Publisher Confirms:</b> Настроен callback для подтверждения доставки сообщений.
     * Если RabbitMQ не может доставить сообщение (NACK), выводится предупреждение.
     * 
     * <p><b>Использование:</b>
     * <pre>{@code
     * rabbitTemplate.convertAndSend(EXCHANGE_NAME, ROUTING_KEY, event);
     * }</pre>
     * 
     * @param connectionFactory Фабрика соединений с RabbitMQ (автоматически инжектируется Spring)
     * @return Настроенный RabbitTemplate
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (!ack) {
                System.out.println("NACK: Message delivery failed! " + cause);
            }
        });
        return rabbitTemplate;
    }

    /**
     * Создает Fanout Exchange для широковещательной рассылки сообщений.
     * 
     * <p><b>Fanout Exchange:</b> Отправляет сообщения во ВСЕ связанные очереди, игнорируя routing key.
     * Используется для событий, которые должны получить все подписчики (например, расчет цены).
     * 
     * <p><b>Параметры конструктора:</b>
     * <ul>
     *   <li>name - имя exchange</li>
     *   <li>durable (true) - exchange сохраняется при перезапуске</li>
     *   <li>autoDelete (false) - exchange не удаляется автоматически</li>
     * </ul>
     * 
     * @return Настроенный Fanout Exchange
     */
    @Bean
    public FanoutExchange reservationFanoutExchange() {
        return new FanoutExchange(FANOUT_EXCHANGE, true, false);
    }
}