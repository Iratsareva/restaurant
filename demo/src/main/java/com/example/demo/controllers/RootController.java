package com.example.demo.controllers;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

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
