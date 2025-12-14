package com.example.demo.repository.Impl;


import com.example.demo.models.TableEntity;
import com.example.demo.repository.AbstractRepository;
import com.example.demo.repository.TableRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;



@Repository
public class TableRepositoryImpl extends AbstractRepository<TableEntity> implements TableRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public TableRepositoryImpl() {
        super(TableEntity.class);
    }

    @Transactional
    @Override
    public TableEntity toggleAvailability(Long id, boolean available) {
        TableEntity table = findById(id);
        if (table != null) {
            table.setAvailable(available);
            return update(table);
        }
        return null;
    }

    @Transactional
    @Override
    public TableEntity updateType(Long id, String type) {
        TableEntity table = findById(id);
        if (table != null) {
            table.setType(type);
            return update(table);
        }
        return null;
    }
}

