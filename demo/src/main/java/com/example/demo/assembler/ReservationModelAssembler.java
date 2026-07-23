package com.example.demo.assembler;


import com.example.Restaurant.dto.ReservationResponse;
import com.example.demo.controllers.ClientController;
import com.example.demo.controllers.ReservationController;
import com.example.demo.controllers.TableController;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * Ассемблер для преобразования ReservationResponse в HATEOAS модели.
 * 
 * <p><b>Назначение:</b> Этот класс реализует паттерн Assembler и отвечает за преобразование
 * DTO объектов (ReservationResponse) в HATEOAS модели (EntityModel/PagedModel) с добавлением
 * ссылок на связанные ресурсы.
 * 
 * <p><b>HATEOAS (Hypermedia as the Engine of Application State):</b>
 * HATEOAS - это принцип REST API, при котором ответы содержат не только данные, но и ссылки
 * на связанные ресурсы. Это позволяет клиенту навигироваться по API без знания точных URL.
 * 
 * <p><b>Архитектурная роль:</b>
 * <ul>
 *   <li><b>Presentation Layer</b> - слой представления, добавление метаданных к ответам</li>
 *   <li><b>Link Building</b> - построение ссылок на связанные ресурсы</li>
 *   <li><b>Response Enrichment</b> - обогащение ответов метаинформацией</li>
 * </ul>
 * 
 * <p><b>Паттерны проектирования:</b>
 * <ul>
 *   <li><b>Assembler Pattern</b> - преобразование между слоями</li>
 *   <li><b>Builder Pattern</b> - построение ссылок через WebMvcLinkBuilder</li>
 * </ul>
 * 
 * <p><b>Ссылки, добавляемые к каждому бронированию:</b>
 * <ul>
 *   <li><b>self</b> - ссылка на само бронирование (/api/reservations/{id})</li>
 *   <li><b>reservations</b> - ссылка на коллекцию всех бронирований</li>
 *   <li><b>client</b> - ссылка на клиента бронирования (/api/clients/{clientId})</li>
 *   <li><b>table</b> - ссылка на столик бронирования (/api/tables/{tableId})</li>
 * </ul>
 * 
 * <p><b>WebMvcLinkBuilder:</b> Используется для построения ссылок на методы контроллеров
 * без жесткого кодирования URL. Spring автоматически генерирует правильные URL на основе
 * аннотаций @RequestMapping.
 * 
 * <p><b>Пример использования:</b>
 * <pre>{@code
 * ReservationResponse response = reservationService.findById(1L);
 * EntityModel<ReservationResponse> model = assembler.toModel(response);
 * // model теперь содержит данные + ссылки в поле _links
 * }</pre>
 * 
 * <p><b>Пример JSON ответа с HATEOAS:</b>
 * <pre>{@code
 * {
 *   "id": 1,
 *   "client": { ... },
 *   "table": { ... },
 *   "_links": {
 *     "self": { "href": "/api/reservations/1" },
 *     "reservations": { "href": "/api/reservations" },
 *     "client": { "href": "/api/clients/1" },
 *     "table": { "href": "/api/tables/5" }
 *   }
 * }
 * }</pre>
 * 
 * @author Restaurant System
 * @version 1.0
 * @see RepresentationModelAssembler
 * @see EntityModel
 * @see CollectionModel
 * @see ReservationResponse
 */
@Component
public class ReservationModelAssembler implements RepresentationModelAssembler<ReservationResponse, EntityModel<ReservationResponse>> {

    @Override
    public EntityModel<ReservationResponse> toModel(ReservationResponse reservation) {
        return EntityModel.of(reservation,
                linkTo(methodOn(ReservationController.class).getReservationById(reservation.getId())).withSelfRel(),
                linkTo(methodOn(ReservationController.class).getAllReservations(null, null, null, null, 0, 10)).withRel("reservations"),
                linkTo(methodOn(ClientController.class).getClientById(reservation.getClient().getId())).withRel("client"),
                linkTo(methodOn(TableController.class).getTableById(reservation.getTable().getId())).withRel("table")
        );
    }

    @Override
    public CollectionModel<EntityModel<ReservationResponse>> toCollectionModel(Iterable<? extends ReservationResponse> entities) {
        List<EntityModel<ReservationResponse>> models = StreamSupport.stream(entities.spliterator(), false)
                .map(this::toModel)
                .collect(Collectors.toList());
        return CollectionModel.of(models,
                linkTo(methodOn(ReservationController.class).getAllReservations(null, null, null, null,  0,10)).withSelfRel());
    }
}