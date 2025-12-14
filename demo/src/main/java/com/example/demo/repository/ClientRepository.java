package com.example.demo.repository;



import com.example.demo.models.Client;

import java.util.List;

public interface ClientRepository {
    Client create(Client client);
    Client findById(Long id);
    List<Client> findAll();
    Client update(Client client);
    void delete(Long id);
}
