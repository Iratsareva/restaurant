package com.example.demo.assembler;


import com.example.Restaurant.dto.ClientResponse;
import com.example.demo.controllers.ClientController;
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
public class ClientModelAssembler implements RepresentationModelAssembler<ClientResponse, EntityModel<ClientResponse>> {

    @Override
    public EntityModel<ClientResponse> toModel(ClientResponse client) {
        return EntityModel.of(client,
                linkTo(methodOn(ClientController.class).getClientById(client.getId())).withSelfRel(),
                linkTo(methodOn(ClientController.class).getAllClients()).withRel("clients")
                // Добавьте link на reservations если нужно: linkTo(methodOn(ReservationController.class).getAllReservations(client.getId(), null, null, 0, 10)).withRel("reservations")
        );
    }

    @Override
    public CollectionModel<EntityModel<ClientResponse>> toCollectionModel(Iterable<? extends ClientResponse> entities) {
        List<EntityModel<ClientResponse>> models = StreamSupport.stream(entities.spliterator(), false)
                .map(this::toModel)
                .collect(Collectors.toList());
        return CollectionModel.of(models,
                linkTo(methodOn(ClientController.class).getAllClients()).withSelfRel());
    }
}
