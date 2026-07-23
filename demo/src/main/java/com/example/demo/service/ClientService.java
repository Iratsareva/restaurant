package com.example.demo.service;



import com.example.Restaurant.dto.ClientRequest;
import com.example.Restaurant.dto.ClientResponse;
import com.example.Restaurant.exception.ResourceNotFoundException;
import com.example.demo.repository.ClientRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


import com.example.demo.models.Client;

/**
 * Сервисный класс для управления бизнес-логикой работы с клиентами.
 * 
 * <p><b>Назначение:</b> Инкапсулирует всю бизнес-логику для работы с клиентами ресторана.
 * Предоставляет CRUD операции (Create, Read, Update, Delete) для управления клиентами.
 * 
 * <p><b>Архитектурная роль:</b>
 * <ul>
 *   <li><b>Business Logic Layer</b> - слой бизнес-логики для работы с клиентами</li>
 *   <li><b>Data Transformation</b> - преобразование между JPA сущностями и DTO</li>
 *   <li><b>Exception Handling</b> - обработка бизнес-исключений</li>
 * </ul>
 * 
 * <p><b>Основные функции:</b>
 * <ul>
 *   <li>Получение списка всех клиентов</li>
 *   <li>Поиск клиента по ID</li>
 *   <li>Создание нового клиента</li>
 *   <li>Обновление данных клиента</li>
 *   <li>Удаление клиента</li>
 * </ul>
 * 
 * <p><b>Паттерны проектирования:</b>
 * <ul>
 *   <li><b>Service Layer Pattern</b> - инкапсуляция бизнес-логики</li>
 *   <li><b>DTO Pattern</b> - использование DTO для передачи данных</li>
 *   <li><b>Repository Pattern</b> - работа с данными через репозиторий</li>
 * </ul>
 * 
 * @author Restaurant System
 * @version 1.0
 * @see ClientRepository
 * @see Client
 * @see ClientRequest
 * @see ClientResponse
 */
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