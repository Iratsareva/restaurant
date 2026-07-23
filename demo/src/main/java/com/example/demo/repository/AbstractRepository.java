package com.example.demo.repository;


import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public abstract class AbstractRepository<T> {

    /**
     * JPA EntityManager для выполнения операций с базой данных.
     * 
     * <p><b>@PersistenceContext:</b> Spring автоматически инжектирует EntityManager,
     * который управляется контейнером и правильно обрабатывает транзакции.
     * 
     * <p><b>protected:</b> Доступен для наследников, но не для внешних классов.
     */
    @PersistenceContext
    protected EntityManager entityManager;

    /**
     * Класс сущности (используется для типизированных запросов).
     * Сохраняется в конструкторе для использования в методах.
     */
    private final Class<T> entityClass;

    /**
     * Конструктор, принимающий класс сущности.
     * 
     * @param entityClass Класс сущности (например, Reservation.class)
     */
    protected AbstractRepository(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    /**
     * Создать новую сущность в базе данных.
     * 
     * <p><b>Описание:</b> Сохраняет новую сущность в БД. После вызова persist()
     * сущность становится управляемой (managed) и получает ID (если используется автоинкремент).
     * 
     * <p><b>Транзакция:</b> Выполняется в транзакции (@Transactional).
     * 
     * @param entity Сущность для сохранения
     * @return Сохраненная сущность (с установленным ID)
     */
    @Transactional
    public T create(T entity) {
        entityManager.persist(entity);
        return entity;
    }

    /**
     * Найти сущность по идентификатору.
     * 
     * <p><b>Описание:</b> Ищет сущность в контексте персистентности (1-й уровень кэша),
     * если не найдена - в базе данных.
     * 
     * <p><b>Особенности:</b>
     * <ul>
     *   <li>Возвращает null, если сущность не найдена</li>
     *   <li>Не требует транзакции для чтения</li>
     * </ul>
     * 
     * @param id Идентификатор сущности
     * @return Найденная сущность или null
     */
    public T findById(Long id) {
        return entityManager.find(entityClass, id);
    }

    /**
     * Обновить существующую сущность в базе данных.
     * 
     * <p><b>Описание:</b> Обновляет сущность в БД. Если сущность не управляемая (detached),
     * merge() создает новую управляемую копию и возвращает ее.
     * 
     * <p><b>Транзакция:</b> Выполняется в транзакции (@Transactional).
     * 
     * <p><b>Важно:</b> Метод возвращает управляемую сущность. Если вы обновляете detached сущность,
     * нужно использовать возвращаемое значение, а не исходный объект.
     * 
     * @param entity Сущность для обновления
     * @return Обновленная управляемая сущность
     */
    @Transactional
    public T update(T entity) {
        return entityManager.merge(entity);
    }

    /**
     * Удалить сущность из базы данных по идентификатору.
     * 
     * <p><b>Описание:</b> Находит сущность по ID и удаляет ее из БД.
     * Если сущность не найдена, операция игнорируется (не выбрасывает исключение).
     * 
     * <p><b>Транзакция:</b> Выполняется в транзакции (@Transactional).
     * 
     * <p><b>Идемпотентность:</b> Метод идемпотентен - повторное удаление несуществующей
     * сущности не вызовет ошибку.
     * 
     * @param id Идентификатор сущности для удаления
     */
    @Transactional
    public void delete(Long id) {
        T entity = findById(id);
        if (entity != null) {
            entityManager.remove(entity);
        }
    }

    /**
     * Получить все сущности данного типа из базы данных.
     * 
     * <p><b>Описание:</b> Выполняет JPQL запрос для получения всех записей из таблицы.
     * 
     * <p><b>JPQL запрос:</b> Использует JPQL (Java Persistence Query Language) синтаксис:
     * "from EntityClassName" вместо SQL "SELECT * FROM table_name".
     * 
     * <p><b>Производительность:</b> Загружает все записи в память. Для больших таблиц
     * рекомендуется использовать пагинацию.
     * 
     * <p><b>Пример JPQL:</b> Для Reservation.class запрос будет: "from Reservation"
     * 
     * @return Список всех сущностей
     */
    public List<T> findAll() {
        TypedQuery<T> query = entityManager.createQuery("from " + entityClass.getName(), entityClass);
        return query.getResultList();
    }
}
