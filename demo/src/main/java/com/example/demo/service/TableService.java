package com.example.demo.service;



import com.example.Restaurant.dto.TableRequest;
import com.example.Restaurant.dto.TableResponse;
import com.example.Restaurant.exception.ResourceNotFoundException;
import com.example.demo.models.TableEntity;
import com.example.demo.repository.TableRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Сервисный класс для управления бизнес-логикой работы со столиками.
 * 
 * <p><b>Назначение:</b> Инкапсулирует всю бизнес-логику для работы со столиками ресторана.
 * Предоставляет CRUD операции и дополнительные методы для управления доступностью и типом столиков.
 * 
 * <p><b>Архитектурная роль:</b>
 * <ul>
 *   <li><b>Business Logic Layer</b> - слой бизнес-логики для работы со столиками</li>
 *   <li><b>Data Transformation</b> - преобразование между JPA сущностями и DTO</li>
 *   <li><b>Business Rules</b> - применение бизнес-правил (например, тип столика по умолчанию)</li>
 * </ul>
 * 
 * <p><b>Основные функции:</b>
 * <ul>
 *   <li>Получение списка всех столиков</li>
 *   <li>Поиск столика по ID</li>
 *   <li>Создание нового столика</li>
 *   <li>Обновление данных столика</li>
 *   <li>Удаление столика</li>
 *   <li>Изменение доступности столика (toggleAvailability)</li>
 *   <li>Обновление типа столика (updateType)</li>
 * </ul>
 * 
 * <p><b>Типы столиков:</b>
 * <ul>
 *   <li><b>STANDARD</b> - стандартный столик (по умолчанию)</li>
 *   <li><b>VIP</b> - VIP столик (более высокая цена)</li>
 *   <li><b>WINDOW</b> - столик у окна</li>
 *   <li>И другие типы, определяемые бизнес-требованиями</li>
 * </ul>
 * 
 * <p><b>Особенности:</b>
 * <ul>
 *   <li>При создании столик автоматически помечается как доступный (isAvailable = true)</li>
 *   <li>Если тип столика не указан, устанавливается "STANDARD"</li>
 *   <li>Тип столика влияет на расчет цены бронирования (через gRPC сервис)</li>
 * </ul>
 * 
 * @author Restaurant System
 * @version 1.0
 * @see TableRepository
 * @see TableEntity
 * @see TableRequest
 * @see TableResponse
 */
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
        table.setAvailable(true);
        table.setType(request.type() != null && !request.type().isBlank()
                ? request.type() 
                : "STANDARD");
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
        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException("Type cannot be empty");
        }
        table = tableRepository.updateType(id, type);
        return toResponse(table);
    }

    private TableResponse toResponse(TableEntity table) {
        String tableType = table.getType() != null && !table.getType().isBlank()
                ? table.getType() 
                : "STANDARD";
        return new TableResponse(table.getId(), table.getNumber(), table.getNumberOfSeats(), tableType, table.isAvailable());
    }
}