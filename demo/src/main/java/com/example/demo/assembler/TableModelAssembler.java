package com.example.demo.assembler;


import com.example.Restaurant.dto.TableResponse;
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
public class TableModelAssembler implements RepresentationModelAssembler<TableResponse, EntityModel<TableResponse>> {

    @Override
    public EntityModel<TableResponse> toModel(TableResponse table) {
        return EntityModel.of(table,
                linkTo(methodOn(TableController.class).getTableById(table.getId())).withSelfRel(),
                linkTo(methodOn(TableController.class).getAllTables()).withRel("tables")
                // Link на reservations: linkTo(methodOn(ReservationController.class).getAllReservations(null, table.getId(), null, 0, 10)).withRel("reservations")
        );
    }

    @Override
    public CollectionModel<EntityModel<TableResponse>> toCollectionModel(Iterable<? extends TableResponse> entities) {
        List<EntityModel<TableResponse>> models = StreamSupport.stream(entities.spliterator(), false)
                .map(this::toModel)
                .collect(Collectors.toList());
        return CollectionModel.of(models,
                linkTo(methodOn(TableController.class).getAllTables()).withSelfRel());
    }
}