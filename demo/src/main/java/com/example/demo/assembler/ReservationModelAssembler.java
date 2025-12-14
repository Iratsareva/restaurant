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