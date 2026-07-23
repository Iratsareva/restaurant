package com.example.demo.filters;


import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.UUID;

/**
 * Servlet Filter для логирования запросов и трассировки (correlation ID).
 * 
 * <p><b>Назначение:</b> Этот фильтр перехватывает все HTTP запросы и добавляет:
 * <ul>
 *   <li>Логирование начала и окончания запросов</li>
 *   <li>Correlation ID для трассировки запросов через систему</li>
 *   <li>Измерение времени выполнения запросов</li>
 * </ul>
 * 
 * <p><b>Архитектурная роль:</b>
 * <ul>
 *   <li><b>Cross-Cutting Concern</b> - сквозная функциональность (логирование, трассировка)</li>
 *   <li><b>Request Interceptor</b> - перехватчик HTTP запросов</li>
 *   <li><b>Observability</b> - обеспечение наблюдаемости системы</li>
 * </ul>
 * 
 * <p><b>Correlation ID (ID корреляции):</b>
 * Уникальный идентификатор, который связывает все логи, связанные с одним запросом.
 * Позволяет отследить путь запроса через всю систему, даже если он проходит через
 * несколько микросервисов.
 * 
 * <p><b>MDC (Mapped Diagnostic Context):</b>
 * Thread-local хранилище для correlation ID. Позволяет автоматически добавлять
 * correlation ID ко всем логам в рамках одного потока выполнения запроса.
 * 
 * <p><b>Особенности:</b>
 * <ul>
 *   <li>Если клиент не передал X-Request-ID, генерируется новый UUID</li>
 *   <li>Correlation ID добавляется в заголовок ответа для клиента</li>
 *   <li>Логируются только запросы к /api/* (не статические ресурсы)</li>
 *   <li>Измеряется время выполнения запроса</li>
 * </ul>
 * 
 * <p><b>Примечание:</b> Класс закомментирован (@Component), так как используется
 * Micrometer Tracing для распределенной трассировки. Этот фильтр можно использовать
 * как альтернативу или дополнение.
 * 
 * <p><b>Пример логов:</b>
 * <pre>
 * [correlationId=abc-123] Request started: GET /api/reservations/1
 * [correlationId=abc-123] Request finished: GET /api/reservations/1 with status 200 in 45ms
 * </pre>
 * 
 * @author Restaurant System
 * @version 1.0
 * @see jakarta.servlet.Filter
 * @see org.slf4j.MDC
 */
// @Component  // Отключено, так как используется Micrometer Tracing
@Order(1)
public class LoggingAndTracingFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(LoggingAndTracingFilter.class);
    private static final String CORRELATION_ID_HEADER = "X-Request-ID";
    private static final String CORRELATION_ID_MDC_KEY = "correlationId";

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        String correlationId = request.getHeader(CORRELATION_ID_HEADER);
        if (!StringUtils.hasText(correlationId)) {
            correlationId = UUID.randomUUID().toString();
        }
        MDC.put(CORRELATION_ID_MDC_KEY, correlationId);
        response.setHeader(CORRELATION_ID_HEADER, correlationId);

        long startTime = System.currentTimeMillis();

        try {
            if (request.getRequestURI().startsWith("/api/")) {
                log.info("Request started: {} {}", request.getMethod(), request.getRequestURI());
            }
            filterChain.doFilter(request, response);

        } finally {
            long duration = System.currentTimeMillis() - startTime;
            if (request.getRequestURI().startsWith("/api/")) {
                log.info("Request finished: {} {} with status {} in {}ms",
                        request.getMethod(), request.getRequestURI(), response.getStatus(), duration);
            }
            MDC.remove(CORRELATION_ID_MDC_KEY);
        }
    }
}
