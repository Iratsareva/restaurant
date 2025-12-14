package com.example.demo.service;



import com.example.Restaurant.dto.TableRequest;
import com.example.Restaurant.dto.TableResponse;
import com.example.Restaurant.exception.ResourceNotFoundException;
import com.example.demo.models.TableEntity;
import com.example.demo.repository.TableRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TableService {
    private final TableRepository tableRepository;

    public TableService(TableRepository tableRepository) {
        this.tableRepository = tableRepository;
    }

    public List<TableResponse> findAll() {
        return tableRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public TableResponse findById(Long id) {
        TableEntity table = tableRepository.findById(id);
        if (table == null) {
            throw new ResourceNotFoundException("Table", id);
        }
        return toResponse(table);
    }

    public TableResponse create(TableRequest request) {
        TableEntity table = new TableEntity();
        table.setNumber(request.number());
        table.setNumberOfSeats(request.numberOfSeats());
        table.setAvailable(true); // По умолчанию доступен
        table = tableRepository.create(table);
        return toResponse(table);
    }

    public TableResponse update(Long id, TableRequest request) {
        TableEntity table = tableRepository.findById(id);
        if (table == null) {
            throw new ResourceNotFoundException("Table", id);
        }
        table.setNumber(request.number());
        table.setNumberOfSeats(request.numberOfSeats());
        table = tableRepository.update(table);
        return toResponse(table);
    }

    public void delete(Long id) {
        TableEntity table = tableRepository.findById(id);
        if (table == null) {
            throw new ResourceNotFoundException("Table", id);
        }
        tableRepository.delete(id);
    }

    public TableResponse toggleAvailability(Long id, boolean available) {
        TableEntity table = tableRepository.findById(id);
        if (table == null) {
            throw new ResourceNotFoundException("Table", id);
        }
        return toResponse(tableRepository.toggleAvailability(id, available));
    }

    public TableResponse updateType(Long id, String type) {
        TableEntity table = tableRepository.findById(id);
        if (table == null) {
            throw new ResourceNotFoundException("Table", id);
        }
        // Бизнес-логика: e.g., валидация type (если нужно, добавьте check на допустимые значения)
        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException("Type cannot be empty");
        }
        table = tableRepository.updateType(id, type);
        return toResponse(table);
    }

    private TableResponse toResponse(TableEntity table) {
        return new TableResponse(table.getId(), table.getNumber(),   table.getNumberOfSeats(),table.getType(), table.isAvailable());
    }
}