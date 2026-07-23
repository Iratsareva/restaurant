package com.example.notification_service.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Обработчик WebSocket соединений для доставки уведомлений.
 * 
 * <p><b>Назначение:</b> Этот класс управляет WebSocket соединениями и отправляет
 * уведомления всем подключенным клиентам в реальном времени.
 * 
 * <p><b>Архитектурная роль:</b>
 * <ul>
 *   <li><b>WebSocket Handler</b> - обработчик WebSocket соединений</li>
 *   <li><b>Session Manager</b> - управление активными соединениями</li>
 *   <li><b>Message Broadcaster</b> - широковещательная отправка сообщений</li>
 * </ul>
 * 
 * <p><b>Жизненный цикл соединения:</b>
 * <ol>
 *   <li><b>afterConnectionEstablished</b> - клиент подключился, сохраняем сессию</li>
 *   <li><b>handleTextMessage</b> - обработка сообщений от клиента (например, PING)</li>
 *   <li><b>broadcast</b> - отправка уведомлений всем клиентам</li>
 *   <li><b>afterConnectionClosed</b> - клиент отключился, удаляем сессию</li>
 * </ol>
 * 
 * <p><b>Потокобезопасность:</b>
 * <ul>
 *   <li>ConcurrentHashMap.newKeySet() - потокобезопасное множество сессий</li>
 *   <li>synchronized (session) - синхронизация при отправке сообщений</li>
 *   <li>Позволяет обрабатывать множественные соединения одновременно</li>
 * </ul>
 * 
 * <p><b>Обработка ошибок:</b>
 * <ul>
 *   <li>Если сессия закрыта - удаляется из множества</li>
 *   <li>Если ошибка отправки - сессия удаляется, обработка продолжается</li>
 *   <li>Все ошибки логируются</li>
 * </ul>
 * 
 * <p><b>PING/PONG:</b> Поддерживает протокол keep-alive. Клиент может отправить
 * "PING", сервер ответит "PONG" для проверки соединения.
 * 
 * <p><b>Широковещательная отправка:</b> Метод broadcast() отправляет сообщение
 * всем подключенным клиентам одновременно. Используется для уведомлений о событиях.
 * 
 * <p><b>Пример использования:</b>
 * <pre>{@code
 * // В NotificationListener при получении события из RabbitMQ:
 * String json = objectMapper.writeValueAsString(message);
 * notificationHandler.broadcast(json);
 * 
 * // Все подключенные WebSocket клиенты получат JSON сообщение
 * }</pre>
 * 
 * <p><b>Ограничения:</b>
 * <ul>
 *   <li>Сессии хранятся в памяти - при перезапуске сервера теряются</li>
 *   <li>Не масштабируется горизонтально (нужен sticky sessions или Redis)</li>
 *   <li>Для продакшена рекомендуется использовать Redis Pub/Sub для масштабирования</li>
 * </ul>
 * 
 * @author Restaurant System
 * @version 1.0
 * @see TextWebSocketHandler
 * @see WebSocketSession
 * @see com.example.notification_service.listener.NotificationListener
 */
@Component
public class NotificationHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(NotificationHandler.class);

    private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
        log.info("Новое WebSocket подключение: id={}, всего активных: {}",
                session.getId(), sessions.size());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        String payload = message.getPayload();
        log.debug("Сообщение от {}: {}", session.getId(), payload);
        if ("PING".equals(payload)) {
            sendMessage(session, new TextMessage("PONG"));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
        log.info("WebSocket отключен: id={}, причина={}, осталось: {}",
                session.getId(), status.getReason(), sessions.size());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.error("Ошибка транспорта для сессии {}: {}",
                session.getId(), exception.getMessage());
        sessions.remove(session);
    }

    public int broadcast(String message) {
        TextMessage textMessage = new TextMessage(message);
        int sent = 0;

        for (WebSocketSession session : sessions) {
            if (sendMessage(session, textMessage)) {
                sent++;
            }
        }
        log.info("Broadcast: отправлено {}/{} клиентам", sent, sessions.size());
        return sent;
    }


    private boolean sendMessage(WebSocketSession session, TextMessage message) {
        if (!session.isOpen()) {
            sessions.remove(session);
            return false;
        }
        try {
            synchronized (session) {
                session.sendMessage(message);
            }
            return true;
        } catch (IOException e) {
            log.warn("Ошибка отправки в сессию {}: {}", session.getId(), e.getMessage());
            sessions.remove(session);
            return false;
        }
    }

    public int getActiveConnections() {
        return sessions.size();
    }
}


