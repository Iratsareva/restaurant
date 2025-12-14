package com.example.demo.repository.Impl;




import com.example.demo.models.Client;
import com.example.demo.repository.AbstractRepository;
import com.example.demo.repository.ClientRepository;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

@Repository
public class ClientRepositoryImpl extends AbstractRepository<Client> implements ClientRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public ClientRepositoryImpl() {
        super(Client.class);
    }
}
