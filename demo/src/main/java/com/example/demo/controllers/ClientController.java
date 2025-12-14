package com.example.demo.controllers;


import com.example.Restaurant.dto.ClientRequest;
import com.example.Restaurant.dto.ClientResponse;
import com.example.Restaurant.endpoints.ClientApi;
import com.example.demo.assembler.ClientModelAssembler;

import com.example.demo.service.ClientService;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
public class ClientController implements ClientApi {
    private final ClientService clientService;
    private final ClientModelAssembler assembler;

    public ClientController(ClientService clientService, ClientModelAssembler assembler) {
        this.clientService = clientService;
        this.assembler = assembler;
    }

    @Override
    public CollectionModel<EntityModel<ClientResponse>> getAllClients() {
        return assembler.toCollectionModel(clientService.findAll());
    }

    @Override
    public EntityModel<ClientResponse> getClientById(Long id) {
        return assembler.toModel(clientService.findById(id));
    }

    @Override
    public ResponseEntity<EntityModel<ClientResponse>> createClient(ClientRequest request) {
        ClientResponse created = clientService.create(request);
        EntityModel<ClientResponse> model = assembler.toModel(created);
        return ResponseEntity.created(model.getRequiredLink("self").toUri())
                .body(model);
    }

    @Override
    public EntityModel<ClientResponse> updateClient(Long id, ClientRequest request) {
        return assembler.toModel(clientService.update(id, request));
    }

    @Override
    public void deleteClient(Long id) {
        clientService.delete(id);
    }
}