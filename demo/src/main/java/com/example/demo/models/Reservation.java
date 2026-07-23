package com.example.demo.models;


import jakarta.persistence.*;
import jakarta.persistence.PrePersist;
import lombok.*;
import java.time.LocalDateTime;

/**
 * JPA сущность для представления бронирования столика в базе данных.
 * 
 * <p><b>Назначение:</b> Этот класс представляет таблицу "reservations" в базе данных PostgreSQL.
 * Используется JPA (Java Persistence API) для маппинга объектов Java на реляционную базу данных.
 * 
 * <p><b>JPA аннотации:</b>
 * <ul>
 *   <li><b>@Entity</b> - указывает, что это JPA сущность</li>
 *   <li><b>@Table</b> - имя таблицы в БД ("reservations")</li>
 *   <li><b>@Id</b> - первичный ключ</li>
 *   <li><b>@GeneratedValue</b> - автоинкремент ID (IDENTITY стратегия)</li>
 *   <li><b>@ManyToOne</b> - связь многие-к-одному с Client и TableEntity</li>
 *   <li><b>@JoinColumn</b> - имя внешнего ключа в таблице</li>
 *   <li><b>@Column</b> - маппинг поля на колонку БД</li>
 *   <li><b>@PrePersist</b> - callback перед сохранением в БД</li>
 * </ul>
 * 
 * <p><b>Связи с другими сущностями:</b>
 * <ul>
 *   <li><b>Client</b> - многие бронирования принадлежат одному клиенту (ManyToOne, LAZY загрузка)</li>
 *   <li><b>TableEntity</b> - многие бронирования относятся к одному столику (ManyToOne, LAZY загрузка)</li>
 * </ul>
 * 
 * <p><b>LAZY загрузка:</b> Связи загружаются лениво (LAZY), что означает, что данные клиента
 * и столика загружаются из БД только при обращении к ним. Это оптимизирует производительность.
 * 
 * <p><b>Статусы бронирования:</b>
 * <ul>
 *   <li><b>PENDING</b> - ожидает подтверждения (начальное состояние)</li>
 *   <li><b>CONFIRMED</b> - подтверждено</li>
 *   <li><b>PAID</b> - оплачено</li>
 *   <li><b>CANCELLED</b> - отменено</li>
 * </ul>
 * 
 * <p><b>Поля:</b>
 * <ul>
 *   <li><b>id</b> - уникальный идентификатор (автоинкремент)</li>
 *   <li><b>client</b> - ссылка на клиента (внешний ключ client_id)</li>
 *   <li><b>table</b> - ссылка на столик (внешний ключ table_id)</li>
 *   <li><b>reservationTime</b> - дата и время бронирования</li>
 *   <li><b>numberOfGuests</b> - количество гостей</li>
 *   <li><b>status</b> - статус бронирования</li>
 *   <li><b>price</b> - рассчитанная цена (по умолчанию 0.0)</li>
 *   <li><b>verdict</b> - вердикт расчета цены (AFFORDABLE/EXPENSIVE)</li>
 *   <li><b>createdAt</b> - время создания записи (автоматически устанавливается через @PrePersist)</li>
 * </ul>
 * 
 * <p><b>Lombok аннотации:</b> Используется Lombok для автоматической генерации:
 * <ul>
 *   <li>@Getter/@Setter - геттеры и сеттеры</li>
 *   <li>@NoArgsConstructor - конструктор без параметров (требуется JPA)</li>
 *   <li>@AllArgsConstructor - конструктор со всеми параметрами</li>
 * </ul>
 * 
 * <p><b>Callback @PrePersist:</b> Метод onCreate() автоматически вызывается перед сохранением
 * новой записи в БД и устанавливает createdAt, если он не был установлен ранее.
 * 
 * @author Restaurant System
 * @version 1.0
 * @see Client
 * @see TableEntity
 * @see jakarta.persistence.Entity
 */
@Entity
@Table(name = "reservations")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "table_id", nullable = false)
    private TableEntity table;

    @Column(name = "reservation_time", nullable = false)
    private LocalDateTime reservationTime;

    @Column(name = "number_of_guests", nullable = false)
    private int numberOfGuests;

    @Column(nullable = false)
    private String status;

    @Column(name = "price")
    private double price = 0.0;

    @Column(name = "verdict")
    private String verdict;

    @Column(name = "created_at", nullable = true, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Callback метод, вызываемый JPA перед сохранением новой сущности в базу данных.
     * 
     * <p><b>Назначение:</b> Автоматически устанавливает время создания записи (createdAt),
     * если оно не было установлено ранее. Это гарантирует, что у каждой записи будет
     * время создания, даже если оно не было явно указано.
     * 
     * <p><b>Использование:</b> Вызывается автоматически JPA провайдером (Hibernate)
     * перед операцией persist() или merge() для новой сущности.
     * 
     * <p><b>Пример:</b>
     * <pre>{@code
     * Reservation reservation = new Reservation();
     * reservation.setClient(client);
     * reservation.setTable(table);
     * // createdAt еще null
     * 
     * reservationRepository.create(reservation);
     * // JPA автоматически вызовет onCreate() перед сохранением
     * // createdAt теперь будет установлен в LocalDateTime.now()
     * }</pre>
     * 
     * <p><b>Примечание:</b> Метод вызывается только для новых сущностей (не для обновлений).
     * Для обновлений используется @PreUpdate, но в данном случае он не нужен.
     */
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
