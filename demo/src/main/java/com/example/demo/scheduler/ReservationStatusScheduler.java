package com.example.demo.scheduler;

import com.example.demo.service.ReservationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Планировщик задач для автоматической отмены просроченных бронирований.
 * 
 * <p><b>Назначение:</b> Этот класс выполняет фоновые задачи по расписанию для автоматической
 * отмены просроченных PENDING бронирований, которые были созданы более 24 часов назад.
 * 
 * <p><b>Архитектурная роль:</b>
 * <ul>
 *   <li><b>Background Job</b> - фоновые задачи, выполняемые по расписанию</li>
 *   <li><b>Automated Business Logic</b> - автоматизация бизнес-правил</li>
 *   <li><b>Maintenance Task</b> - задача обслуживания системы</li>
 * </ul>
 * 
 * <p><b>Spring Scheduling:</b> Использует аннотацию @Scheduled для выполнения задач по расписанию.
 * Требует включения @EnableScheduling в конфигурации приложения.
 * 
 * <p><b>Cron выражение:</b> "0 0 */6 * * *" означает:
 * <ul>
 *   <li>0 секунд</li>
 *   <li>0 минут</li>
 *   <li>каждые 6 часов (*/6)</li>
 *   <li>каждый день месяца (*)</li>
 *   <li>каждый месяц (*)</li>
 *   <li>каждый день недели (*)</li>
 * </ul>
 * То есть задача выполняется каждые 6 часов: в 00:00, 06:00, 12:00, 18:00.
 * 
 * <p><b>Бизнес-логика:</b>
 * <ul>
 *   <li>Находит все бронирования со статусом PENDING</li>
 *   <li>Проверяет время создания (createdAt)</li>
 *   <li>Если createdAt старше 24 часов - автоматически отменяет (статус → CANCELLED)</li>
 *   <li>Публикует событие ReservationStatusChangedEvent для каждого отмененного бронирования</li>
 * </ul>
 * 
 * <p><b>Обработка ошибок:</b> Если при обработке одного бронирования произошла ошибка,
 * обработка продолжается для остальных. Все ошибки логируются.
 * 
 * <p><b>Преимущества автоматизации:</b>
 * <ul>
 *   <li>Освобождает столики, которые не были подтверждены</li>
 *   <li>Улучшает доступность столиков для других клиентов</li>
 *   <li>Не требует ручного вмешательства администратора</li>
 * </ul>
 * 
 * @author Restaurant System
 * @version 1.0
 * @see ReservationService#cancelExpiredPendingReservations()
 * @see org.springframework.scheduling.annotation.Scheduled
 */
@Component
public class ReservationStatusScheduler {
    private static final Logger log = LoggerFactory.getLogger(ReservationStatusScheduler.class);
    private final ReservationService reservationService;

    public ReservationStatusScheduler(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @Scheduled(cron = "0 0 */6 * * *")
    public void cancelExpiredReservations() {
        log.info("Запуск проверки просроченных бронирований...");
        try {
            int cancelledCount = reservationService.cancelExpiredPendingReservations();
            if (cancelledCount == 0) {
                log.debug("Просроченных бронирований не найдено");
            }
        } catch (Exception e) {
            log.error("Ошибка при выполнении задачи отмены просроченных бронирований", e);
        }
    }
}




