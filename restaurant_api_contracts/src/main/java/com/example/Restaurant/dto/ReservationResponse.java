package com.example.Restaurant.dto;

import java.time.LocalDateTime;


import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;
import java.util.Objects;

/**
 * DTO (Data Transfer Object) для ответа с информацией о бронировании.
 * 
 * <p><b>Назначение:</b> Этот класс представляет данные бронирования, которые отправляются клиенту
 * в ответ на запрос. Расширяет RepresentationModel для поддержки HATEOAS (Hypermedia as the Engine of Application State).
 * 
 * <p><b>HATEOAS:</b> Благодаря наследованию от RepresentationModel, этот класс может содержать
 * ссылки на связанные ресурсы (self, client, table, reservations), что позволяет клиенту
 * навигироваться по API без знания точных URL.
 * 
 * <p><b>Аннотация @Relation:</b> Указывает Spring HATEOAS, как называть коллекции и отдельные элементы
 * в JSON ответах:
 * - collectionRelation = "reservations" - имя для коллекции
 * - itemRelation = "reservation" - имя для отдельного элемента
 * 
 * <p><b>Структура данных:</b> Содержит полную информацию о бронировании, включая вложенные объекты
 * ClientResponse и TableResponse, что позволяет клиенту получить всю необходимую информацию
 * в одном запросе.
 * 
 * <p><b>Использование:</b> Создается в методе toResponse() сервиса ReservationService
 * и преобразуется в EntityModel через ReservationModelAssembler для добавления HATEOAS ссылок.
 * 
 * <p><b>Пример JSON ответа:</b>
 * <pre>{@code
 * {
 *   "id": 1,
 *   "client": { "id": 1, "name": "Иван", ... },
 *   "table": { "id": 5, "number": "T-5", ... },
 *   "reservationTime": "2024-01-15T19:00:00",
 *   "numberOfGuests": 4,
 *   "status": "PENDING",
 *   "price": 800.0,
 *   "_links": {
 *     "self": { "href": "/api/reservations/1" },
 *     "client": { "href": "/api/clients/1" },
 *     "table": { "href": "/api/tables/5" }
 *   }
 * }
 * }</pre>
 * 
 * @author Restaurant System
 * @version 1.0
 * @see RepresentationModel
 * @see ReservationRequest
 * @see ClientResponse
 * @see TableResponse
 * @see com.example.demo.assembler.ReservationModelAssembler
 */
@Relation(collectionRelation = "reservations", itemRelation = "reservation")
public class ReservationResponse extends RepresentationModel<ReservationResponse> {
    private final Long id;
    private final ClientResponse client;
    private final TableResponse table;
    private final LocalDateTime reservationTime;
    private final int numberOfGuests;
    private final String status;
    private final double price;


    /**
     * Конструктор для создания объекта ReservationResponse.
     * 
     * @param id Уникальный идентификатор бронирования
     * @param client Объект с информацией о клиенте (ClientResponse)
     * @param table Объект с информацией о столике (TableResponse)
     * @param reservationTime Дата и время бронирования
     * @param numberOfGuests Количество гостей
     * @param status Статус бронирования (PENDING, CONFIRMED, PAID, CANCELLED)
     * @param price Рассчитанная цена бронирования
     */
    public ReservationResponse(Long id, ClientResponse client, TableResponse table, LocalDateTime reservationTime, int numberOfGuests, String status, double price) {
        this.id = id;
        this.client = client;
        this.table = table;
        this.reservationTime = reservationTime;
        this.numberOfGuests = numberOfGuests;
        this.status = status;
        this.price = price;
    }

    /** @return Уникальный идентификатор бронирования */
    public Long getId() { return id; }
    
    /** @return Объект с информацией о клиенте */
    public ClientResponse getClient() { return client; }
    
    /** @return Объект с информацией о столике */
    public TableResponse getTable() { return table; }
    
    /** @return Дата и время бронирования */
    public LocalDateTime getReservationTime() { return reservationTime; }
    
    /** @return Количество гостей */
    public int getNumberOfGuests() { return numberOfGuests; }
    
    /** 
     * @return Статус бронирования. Возможные значения:
     *         - PENDING: ожидает подтверждения
     *         - CONFIRMED: подтверждено
     *         - PAID: оплачено
     *         - CANCELLED: отменено
     */
    public String getStatus() { return status; }
    
    /** @return Рассчитанная цена бронирования в рублях */
    public double getPrice() { return price; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ReservationResponse that = (ReservationResponse) o;
        return numberOfGuests == that.numberOfGuests && Objects.equals(id, that.id) && Objects.equals(client, that.client) &&
                Objects.equals(table, that.table) && Objects.equals(reservationTime, that.reservationTime) && Objects.equals(status, that.status) && Objects.equals(price, that.price);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), id, client, table, reservationTime, numberOfGuests, status, price);
    }
}

