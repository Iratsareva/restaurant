package com.example.demo.controllers;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * Корневой контроллер для предоставления навигации по API.
 * 
 * <p><b>Назначение:</b> Этот контроллер обрабатывает запросы к корневому эндпоинту /api
 * и возвращает HATEOAS ссылки на все основные ресурсы API. Это позволяет клиенту
 * "открыть" API и увидеть доступные ресурсы без знания точных URL.
 * 
 * <p><b>Архитектурная роль:</b>
 * <ul>
 *   <li><b>API Discovery</b> - обнаружение доступных ресурсов API</li>
 *   <li><b>Entry Point</b> - точка входа в API</li>
 *   <li><b>Navigation</b> - навигация по API через HATEOAS</li>
 * </ul>
 * 
 * <p><b>HATEOAS принцип:</b> Реализует принцип "API как машина состояний", где клиент
 * может навигироваться по API, следуя ссылкам, без знания структуры URL заранее.
 * 
 * <p><b>Предоставляемые ссылки:</b>
 * <ul>
 *   <li><b>clients</b> - ссылка на коллекцию клиентов</li>
 *   <li><b>tables</b> - ссылка на коллекцию столиков</li>
 *   <li><b>reservations</b> - ссылка на коллекцию бронирований</li>
 *   <li><b>documentation</b> - ссылка на Swagger документацию</li>
 * </ul>
 * 
 * <p><b>Пример использования:</b>
 * <pre>
 * GET /api
 * 
 * Ответ:
 * {
 *   "_links": {
 *     "clients": { "href": "/api/clients" },
 *     "tables": { "href": "/api/tables" },
 *     "reservations": { "href": "/api/reservations?page=0&size=10" },
 *     "documentation": { "href": "/swagger-ui/index.html" }
 *   }
 * }
 * </pre>
 * 
 * <p><b>Преимущества:</b>
 * <ul>
 *   <li>Клиент может "открыть" API и увидеть доступные ресурсы</li>
 *   <li>Не нужно знать точные URL заранее</li>
 *   <li>Легче поддерживать при изменении структуры URL</li>
 * </ul>
 * 
 * @author Restaurant System
 * @version 1.0
 * @see RepresentationModel
 * @see Link
 */
@RestController
@RequestMapping("/api")
public class RootController {
    @GetMapping
    public RepresentationModel<?> getRoot() {
        RepresentationModel<?> rootModel = new RepresentationModel<>();
        rootModel.add(
                linkTo(methodOn(ClientController.class).getAllClients()).withRel("clients"),
                linkTo(methodOn(TableController.class).getAllTables()).withRel("tables"),
                linkTo(methodOn(ReservationController.class).getAllReservations(null, null, null, null, 0, 10)).withRel("reservations"),
                Link.of("/swagger-ui/index.html", "documentation")
        );
        return rootModel;
    }
}
