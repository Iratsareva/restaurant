package com.example.demo.graphql;


import com.example.Restaurant.dto.*;
import com.example.demo.service.ReservationService;
import com.netflix.graphql.dgs.*;
import org.springframework.stereotype.Component;

/**
 * GraphQL Data Fetcher для работы с бронированиями.
 * 
 * <p><b>Назначение:</b> Этот класс реализует GraphQL API для работы с бронированиями,
 * используя Netflix DGS (Domain Graph Service) framework. Предоставляет альтернативный
 * интерфейс к REST API для тех клиентов, которые предпочитают GraphQL.
 * 
 * <p><b>GraphQL vs REST:</b>
 * <ul>
 *   <li><b>REST</b> - фиксированные эндпоинты, клиент получает все поля ресурса</li>
 *   <li><b>GraphQL</b> - один эндпоинт (/graphql), клиент запрашивает только нужные поля</li>
 *   <li><b>GraphQL преимущества:</b> меньше переданных данных, гибкость запросов, типизация</li>
 * </ul>
 * 
 * <p><b>Архитектурная роль:</b>
 * <ul>
 *   <li><b>GraphQL Layer</b> - слой GraphQL API</li>
 *   <li><b>Data Fetcher</b> - получение данных для GraphQL запросов</li>
 *   <li><b>Adapter</b> - адаптация между GraphQL и бизнес-логикой</li>
 * </ul>
 * 
 * <p><b>Netflix DGS аннотации:</b>
 * <ul>
 *   <li><b>@DgsComponent</b> - помечает класс как GraphQL компонент</li>
 *   <li><b>@DgsQuery</b> - методы для GraphQL запросов (чтение данных)</li>
 *   <li><b>@DgsMutation</b> - методы для GraphQL мутаций (изменение данных)</li>
 *   <li><b>@InputArgument</b> - параметры GraphQL запроса/мутации</li>
 * </ul>
 * 
 * <p><b>Схема GraphQL:</b> Определена в файле schema.graphqls. Методы этого класса
 * реализуют операции, описанные в схеме.
 * 
 * <p><b>Пример GraphQL запроса:</b>
 * <pre>{@code
 * query {
 *   reservationById(id: "1") {
 *     id
 *     reservationTime
 *     status
 *     price
 *     client {
 *       name
 *       email
 *     }
 *     table {
 *       number
 *       type
 *     }
 *   }
 * }
 * }</pre>
 * 
 * <p><b>Пример GraphQL мутации:</b>
 * <pre>{@code
 * mutation {
 *   createReservation(input: {
 *     clientId: "1"
 *     tableId: "5"
 *     reservationTime: "2024-01-15T19:00:00"
 *     numberOfGuests: 4
 *   }) {
 *     id
 *     status
 *     price
 *   }
 * }
 * }</pre>
 * 
 * <p><b>Делегирование:</b> Все методы делегируют вызовы ReservationService,
 * который содержит всю бизнес-логику. Data Fetcher только адаптирует GraphQL
 * запросы к вызовам сервиса.
 * 
 * @author Restaurant System
 * @version 1.0
 * @see ReservationService
 * @see com.netflix.graphql.dgs
 * @see schema.graphqls
 */
@DgsComponent
public class ReservationDataFetcher {

    private final ReservationService reservationService;

    public ReservationDataFetcher(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @DgsQuery
    public ReservationResponse reservationById(@InputArgument Long id) {
        return reservationService.findById(id);
    }

    @DgsQuery
    public PagedResponse<ReservationResponse> reservations(
            @InputArgument("clientId") Long clientId,
            @InputArgument("tableId") Long tableId,
            @InputArgument("status") String status,
            @InputArgument("tableType") String tableType,
            @InputArgument("page") Integer page,
            @InputArgument("size") Integer size
    ) {
        if (page == null) page = 0;
        if (size == null) size = 10;
        return reservationService.findAll(clientId, tableId, status, tableType, page, size);
    }

    @DgsMutation
    public ReservationResponse createReservation(@InputArgument("input") ReservationRequest input) {
        return reservationService.create(input);
    }

    @DgsMutation
    public ReservationResponse updateReservation(@InputArgument Long id, @InputArgument("input") UpdateReservationRequest input) {
        return reservationService.update(id, input);
    }

    @DgsMutation
    public ReservationResponse updateReservationStatus(@InputArgument Long id, @InputArgument String status) {
        return reservationService.changeStatus(id, status);
    }

    @DgsMutation
    public Long deleteReservation(@InputArgument Long id) {
        reservationService.delete(id);
        return id;
    }
}
