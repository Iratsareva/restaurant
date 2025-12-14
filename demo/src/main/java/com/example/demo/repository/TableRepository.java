package com.example.demo.repository;


import com.example.demo.models.TableEntity;

import java.util.List;

public interface TableRepository {
    TableEntity create(TableEntity table);
    TableEntity findById(Long id);
    List<TableEntity> findAll();
    TableEntity update(TableEntity table);
    void delete(Long id);
    TableEntity toggleAvailability(Long id, boolean available);


    TableEntity updateType(Long id, String type);
}
