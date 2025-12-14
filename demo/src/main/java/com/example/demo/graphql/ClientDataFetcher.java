package com.example.demo.graphql;


import com.example.Restaurant.dto.ClientRequest;
import com.example.Restaurant.dto.ClientResponse;
import com.example.demo.service.ClientService;
import com.netflix.graphql.dgs.*;
import org.springframework.stereotype.Component;

import java.util.List;


import java.util.List;


import com.example.Restaurant.dto.ClientRequest;
import com.example.Restaurant.dto.ClientResponse;
import com.example.demo.service.ClientService;
import com.netflix.graphql.dgs.*;
import org.springframework.stereotype.Component;

import java.util.List;

@DgsComponent
public class ClientDataFetcher {

    private final ClientService clientService;

    public ClientDataFetcher(ClientService clientService) {
        this.clientService = clientService;
    }

    // ---------- QUERIES ----------
    @DgsQuery
    public List<ClientResponse> clients() {
        return clientService.findAll();
    }

    @DgsQuery
    public ClientResponse clientById(@InputArgument Long id) {
        return clientService.findById(id);
    }

    // ---------- MUTATIONS ----------
    @DgsMutation
    public ClientResponse createClient(@InputArgument("input") ClientRequest input) {
        return clientService.create(input);
    }

    @DgsMutation
    public ClientResponse updateClient(@InputArgument Long id, @InputArgument("input") ClientRequest input) {
        return clientService.update(id, input);
    }

    @DgsMutation
    public Long deleteClient(@InputArgument Long id) {
        clientService.delete(id);
        return id;
    }
}
