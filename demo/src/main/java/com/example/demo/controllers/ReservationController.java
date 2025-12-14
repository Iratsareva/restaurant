package com.example.demo.controllers;



import com.example.Restaurant.dto.PagedResponse;
import com.example.Restaurant.dto.ReservationRequest;
import com.example.Restaurant.dto.ReservationResponse;
import com.example.Restaurant.dto.UpdateReservationRequest;
import com.example.Restaurant.endpoints.ReservationApi;
import com.example.demo.assembler.ReservationModelAssembler;
import com.example.demo.service.ReservationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
public class ReservationController implements ReservationApi {
    private final ReservationService reservationService;
    private final ReservationModelAssembler assembler;
    private final PagedResourcesAssembler<ReservationResponse> pagedAssembler;

    public ReservationController(ReservationService reservationService, ReservationModelAssembler assembler, PagedResourcesAssembler<ReservationResponse> pagedAssembler) {
        this.reservationService = reservationService;
        this.assembler = assembler;
        this.pagedAssembler = pagedAssembler;
    }

    @Override
    public EntityModel<ReservationResponse> getReservationById(Long id) {
        return assembler.toModel(reservationService.findById(id));
    }

    @Override
    public PagedModel<EntityModel<ReservationResponse>> getAllReservations(Long clientId, Long tableId, String status, String tableType, int page, int size) {
        PagedResponse<ReservationResponse> pagedResponse = reservationService.findAll(clientId, tableId, status,tableType,  page, size);
        // Адаптер для PagedResourcesAssembler (поскольку у вас manual пагинация, создаем Page)
        Page<ReservationResponse> pageImpl = new PageImpl<>(pagedResponse.content(), PageRequest.of(page, size), pagedResponse.totalElements());
        return pagedAssembler.toModel(pageImpl, assembler);
    }

    @Override
    public ResponseEntity<EntityModel<ReservationResponse>> createReservation(ReservationRequest request) {
        ReservationResponse created = reservationService.create(request);
        EntityModel<ReservationResponse> model = assembler.toModel(created);
        return ResponseEntity.created(model.getRequiredLink("self").toUri())
                .body(model);
    }

    @Override
    public EntityModel<ReservationResponse> updateReservation(Long id, UpdateReservationRequest request) {
        return assembler.toModel(reservationService.update(id, request));
    }

    @Override
    public void deleteReservation(Long id) {
        reservationService.delete(id);
    }
}