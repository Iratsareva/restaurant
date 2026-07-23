package com.example.notification_service.config;


import com.example.notification_service.websocket.NotificationHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * Конфигурация WebSocket для сервиса уведомлений.
 * 
 * <p><b>Назначение:</b> Этот класс настраивает WebSocket соединения для доставки
 * уведомлений клиентам в реальном времени.
 * 
 * <p><b>WebSocket vs HTTP:</b>
 * <ul>
 *   <li><b>HTTP</b> - запрос-ответ, клиент должен постоянно опрашивать сервер (polling)</li>
 *   <li><b>WebSocket</b> - постоянное двустороннее соединение, сервер может отправлять данные в любой момент</li>
 * </ul>
 * 
 * <p><b>Архитектурная роль:</b>
 * <ul>
 *   <li><b>Real-time Communication</b> - обеспечение связи в реальном времени</li>
 *   <li><b>Push Notifications</b> - отправка уведомлений от сервера к клиенту</li>
 *   <li><b>Event Streaming</b> - потоковая передача событий</li>
 * </ul>
 * 
 * <p><b>Паттерн использования:</b>
 * <pre>
 * 1. Клиент открывает WebSocket соединение: ws://host/ws/notifications
 * 2. Сервер сохраняет соединение в памяти
 * 3. При событии (например, создание бронирования) сервер отправляет уведомление
 * 4. Все подключенные клиенты получают уведомление мгновенно
 * </pre>
 * 
 * <p><b>CORS:</b> setAllowedOrigins("*") разрешает подключения с любых доменов.
 * В продакшене рекомендуется ограничить список разрешенных доменов.
 * 
 * <p><b>Эндпоинт:</b> /ws/notifications - URL для подключения WebSocket клиентов.
 * 
 * @author Restaurant System
 * @version 1.0
 * @see NotificationHandler
 * @see org.springframework.web.socket.config.annotation.EnableWebSocket
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final NotificationHandler notificationHandler;

    public WebSocketConfig(NotificationHandler notificationHandler) {
        this.notificationHandler = notificationHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(notificationHandler, "/ws/notifications")
                .setAllowedOrigins("*");
    }
}
