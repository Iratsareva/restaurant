package com.example.demo.repository.Impl;

import com.example.demo.models.Reservation;
import com.example.demo.repository.AbstractRepository;
import com.example.demo.repository.ReservationRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Реализация репозитория для работы с бронированиями в базе данных.
 * 
 * <p><b>Назначение:</b> Этот класс расширяет AbstractRepository и реализует интерфейс ReservationRepository,
 * предоставляя специфичные методы для работы с бронированиями, которые не входят в стандартный CRUD набор.
 * 
 * <p><b>Архитектурная роль:</b>
 * <ul>
 *   <li><b>Data Access Layer</b> - слой доступа к данным для бронирований</li>
 *   <li><b>Query Implementation</b> - реализация сложных запросов к БД</li>
 *   <li><b>Business Logic Support</b> - поддержка бизнес-логики через специализированные запросы</li>
 * </ul>
 * 
 * <p><b>Наследование:</b> Наследуется от AbstractRepository<Reservation>, что дает доступ к базовым
 * CRUD операциям (create, findById, update, delete, findAll).
 * 
 * <p><b>Специфичные методы:</b>
 * <ul>
 *   <li><b>findByClientId</b> - найти все бронирования клиента</li>
 *   <li><b>findByTableId</b> - найти все бронирования столика</li>
 *   <li><b>findByStatus</b> - найти все бронирования с определенным статусом</li>
 *   <li><b>findByTableType</b> - найти все бронирования столиков определенного типа</li>
 *   <li><b>findOverlapping</b> - найти пересекающиеся по времени бронирования (для проверки конфликтов)</li>
 * </ul>
 * 
 * <p><b>JPQL запросы:</b> Использует JPQL (Java Persistence Query Language) для написания
 * объектно-ориентированных запросов вместо SQL. JPQL работает с сущностями, а не с таблицами.
 * 
 * <p><b>Особенности метода findOverlapping:</b>
 * <ul>
 *   <li>Выполняет сложную логику проверки пересечений по времени</li>
 *   <li>Использует комбинацию JPQL и Java Streams для фильтрации</li>
 *   <li>Исключает отмененные бронирования (status != 'CANCELLED')</li>
 *   <li>Учитывает длительность бронирования (2 часа)</li>
 * </ul>
 * 
 * <p><b>Производительность:</b> Методы загружают данные в память. Для больших объемов
 * рекомендуется оптимизация через индексы БД и пагинацию на уровне SQL.
 * 
 * @author Restaurant System
 * @version 1.0
 * @see AbstractRepository
 * @see ReservationRepository
 * @see Reservation
 */
@Repository
public class ReservationRepositoryImpl extends AbstractRepository<Reservation> implements ReservationRepository {

    /**
     * JPA EntityManager для выполнения JPQL запросов.
     * Наследуется от AbstractRepository, но переопределяется для явного доступа.
     */
    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Конструктор, передающий класс сущности в родительский класс.
     */
    public ReservationRepositoryImpl() {
        super(Reservation.class);
    }

    /**
     * Найти все бронирования для указанного клиента.
     * 
     * <p><b>JPQL запрос:</b> "SELECT r FROM Reservation r WHERE r.client.id = :clientId"
     * 
     * <p><b>Особенности:</b>
     * <ul>
     *   <li>Использует навигацию по связи (r.client.id) вместо JOIN</li>
     *   <li>Параметризованный запрос (:clientId) для защиты от SQL инъекций</li>
     * </ul>
     * 
     * @param clientId Идентификатор клиента
     * @return Список всех бронирований клиента
     */
    @Override
    public List<Reservation> findByClientId(Long clientId) {
        TypedQuery<Reservation> query = entityManager.createQuery(
                "SELECT r FROM Reservation r WHERE r.client.id = :clientId", Reservation.class);
        return query.setParameter("clientId", clientId).getResultList();
    }

    /**
     * Найти все бронирования для указанного столика.
     * 
     * <p><b>JPQL запрос:</b> "SELECT r FROM Reservation r WHERE r.table.id = :tableId"
     * 
     * @param tableId Идентификатор столика
     * @return Список всех бронирований столика
     */
    @Override
    public List<Reservation> findByTableId(Long tableId) {
        TypedQuery<Reservation> query = entityManager.createQuery(
                "SELECT r FROM Reservation r WHERE r.table.id = :tableId", Reservation.class);
        return query.setParameter("tableId", tableId).getResultList();
    }

    /**
     * Найти все бронирования с указанным статусом.
     * 
     * <p><b>Использование:</b> Используется для поиска всех PENDING бронирований
     * при автоматической отмене просроченных.
     * 
     * @param status Статус бронирования (PENDING, CONFIRMED, PAID, CANCELLED)
     * @return Список всех бронирований с указанным статусом
     */
    @Override
    public List<Reservation> findByStatus(String status) {
        TypedQuery<Reservation> query = entityManager.createQuery(
                "SELECT r FROM Reservation r WHERE r.status = :status", Reservation.class);
        return query.setParameter("status", status).getResultList();
    }

    /**
     * Найти все бронирования, которые пересекаются по времени с указанным интервалом.
     * 
     * <p><b>Назначение:</b> Используется для проверки конфликтов при создании или обновлении
     * бронирования. Проверяет, что столик не забронирован на указанное время.
     * 
     * <p><b>Алгоритм:</b>
     * <ol>
     *   <li>JPQL запрос находит все бронирования столика, которые начинаются до конца интервала</li>
     *   <li>Исключаются отмененные бронирования (status != 'CANCELLED')</li>
     *   <li>Java Stream фильтрует результаты, проверяя пересечение по времени</li>
     *   <li>Учитывается длительность бронирования (2 часа)</li>
     * </ol>
     * 
     * <p><b>Логика пересечения:</b>
     * Два интервала пересекаются, если:
     * <pre>
     * start1 < end2 AND start2 < end1
     * где end = start + 2 часа
     * </pre>
     * 
     * <p><b>Пример:</b>
     * <pre>
     * Существующее бронирование: 19:00 - 21:00
     * Новое бронирование: 20:00 - 22:00
     * Результат: пересекаются (конфликт)
     * </pre>
     * 
     * <p><b>Производительность:</b> Выполняет фильтрацию в памяти через Streams.
     * Для больших объемов данных рекомендуется оптимизировать через SQL.
     * 
     * @param tableId Идентификатор столика
     * @param start Начало интервала времени
     * @param end Конец интервала времени
     * @return Список пересекающихся бронирований
     */
    @Override
    public List<Reservation> findOverlapping(Long tableId, LocalDateTime start, LocalDateTime end) {
        // JPQL запрос: найти все бронирования столика, которые начинаются до конца интервала
        // и не отменены
        TypedQuery<Reservation> query = entityManager.createQuery(
                "SELECT r FROM Reservation r " +
                        "WHERE r.table.id = :tableId " +
                        "AND r.status != 'CANCELLED' " +
                        "AND r.reservationTime < :end", Reservation.class);
        List<Reservation> candidates = query.setParameter("tableId", tableId)
                .setParameter("end", end)
                .getResultList();
        
        // Фильтрация в памяти: проверка пересечения с учетом длительности (2 часа)
        Duration twoHours = Duration.ofHours(2);
        return candidates.stream()
                .filter(r -> r.getReservationTime().plus(twoHours).isAfter(start))
                .collect(Collectors.toList());
    }

    /**
     * Найти все бронирования столиков указанного типа.
     * 
     * <p><b>Использование:</b> Используется для фильтрации бронирований по типу столика
     * (например, только VIP столики).
     * 
     * <p><b>JPQL запрос:</b> Использует навигацию по связи (r.table.type) для доступа
     * к типу столика через связь ManyToOne.
     * 
     * @param type Тип столика (STANDARD, VIP, WINDOW и т.д.)
     * @return Список всех бронирований столиков указанного типа
     */
    @Override
    public List<Reservation> findByTableType(String type) {
        TypedQuery<Reservation> query = entityManager.createQuery(
                "SELECT r FROM Reservation r WHERE r.table.type = :type", Reservation.class);
        return query.setParameter("type", type).getResultList();
    }
}

