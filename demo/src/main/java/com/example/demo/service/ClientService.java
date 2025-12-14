package com.example.demo.service;



import com.example.Restaurant.dto.ClientRequest;
import com.example.Restaurant.dto.ClientResponse;
import com.example.Restaurant.exception.ResourceNotFoundException;
import com.example.demo.repository.ClientRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


import com.example.demo.models.Client;


@Service
public class ClientService {
    private final ClientRepository clientRepository;

    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    public List<ClientResponse> findAll() {
        return clientRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public ClientResponse findById(Long id) {
        Client client = clientRepository.findById(id);
        if (client == null) {
            throw new ResourceNotFoundException("Client", id);
        }
        return toResponse(client);
    }

    public ClientResponse create(ClientRequest request) {
        Client client = new Client();
        client.setName(request.name());
        client.setEmail(request.email());
        client.setPhone(request.phone());
        client = clientRepository.create(client);
        return toResponse(client);
    }

    public ClientResponse update(Long id, ClientRequest request) {
        Client client = clientRepository.findById(id);
        if (client == null) {
            throw new ResourceNotFoundException("Client", id);
        }
        client.setName(request.name());
        client.setEmail(request.email());
        client.setPhone(request.phone());
        client = clientRepository.update(client);
        return toResponse(client);
    }

    public void delete(Long id) {
        Client client = clientRepository.findById(id);
        if (client == null) {
            throw new ResourceNotFoundException("Client", id);
        }
        clientRepository.delete(id);
    }

    private ClientResponse toResponse(Client client) {
        return new ClientResponse(client.getId(), client.getName(), client.getEmail(), client.getPhone());
    }
}