package com.example.demo.service;



import com.example.Restaurant.dto.*;
import com.example.Restaurant.exception.ResourceNotFoundException;

import com.example.demo.config.RabbitMQConfig;
import com.example.demo.models.Client;
import com.example.demo.models.Reservation;
import com.example.demo.models.TableEntity;
import com.example.demo.repository.ClientRepository;
import com.example.demo.repository.ReservationRepository;
import com.example.demo.repository.TableRepository;
import org.example.restaurant.events.ReservationCreatedEvent;
import org.example.restaurant.events.ReservationDeletedEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import io.grpc.StatusRuntimeException;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.example.reservationprice.PriceRequest;
import org.example.reservationprice.PriceResponse;
import org.example.reservationprice.ReservationPriceServiceGrpc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.example.restaurant.events.ReservationPricedEvent;

@Service
public class ReservationService {
    private static final Logger log = LoggerFactory.getLogger(ReservationService.class);
    private final ReservationRepository reservationRepository;
    private final TableRepository tableRepository;
    private final ClientRepository clientRepository;
    private final ClientService clientService;
    private final TableService tableService;
    private final RabbitTemplate rabbitTemplate; // Добавляем RabbitTemplate



    private static final Duration RESERVATION_DURATION = Duration.ofHours(2); // Продолжительность брони

    public ReservationService(ReservationRepository reservationRepository, TableRepository tableRepository, ClientRepository clientRepository, ClientService clientService, TableService tableService, RabbitTemplate rabbitTemplate) {
        this.reservationRepository = reservationRepository;
        this.tableRepository = tableRepository;
        this.clientRepository = clientRepository;
        this.clientService = clientService;
        this.tableService = tableService;
        this.rabbitTemplate = rabbitTemplate;
    }

    public PagedResponse<ReservationResponse> findAll(Long clientId, Long tableId, String status,String tableType, int page, int size) {
        List<Reservation> reservations;
        if (clientId != null) {
            reservations = reservationRepository.findByClientId(clientId);
        } else if (tableId != null) {
            reservations = reservationRepository.findByTableId(tableId);
        } else if (status != null) {
            reservations = reservationRepository.findByStatus(status);
        }else if (tableType != null) {
            reservations = reservationRepository.findByTableType(tableType);
        } else {
            reservations = reservationRepository.findAll();
        }

        int totalElements = reservations.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);
        int from = page * size;
        int to = Math.min(from + size, totalElements);
        List<Reservation> pageContent = reservations.subList(from, to);

        List<ReservationResponse> content = pageContent.stream().map(this::toResponse).collect(Collectors.toList());
        boolean last = page >= totalPages - 1;

        return new PagedResponse<>(content, page, size, totalElements, totalPages, last);
    }

    public ReservationResponse findById(Long id) {
        Reservation reservation = reservationRepository.findById(id);
        if (reservation == null) {
            throw new ResourceNotFoundException("Reservation", id);
        }
        return toResponse(reservation);
    }






    public ReservationResponse create(ReservationRequest request) {
        Client clientEntity = clientRepository.findById(request.clientId());
        if (clientEntity == null) {
            throw new ResourceNotFoundException("Client", request.clientId());
        }

        TableEntity tableEntity = tableRepository.findById(request.tableId());
        if (tableEntity == null) {
            throw new ResourceNotFoundException("Table", request.tableId());
        }

        if (!tableEntity.isAvailable()) {
            throw new RuntimeException("Table not available");
        }
        if (request.numberOfGuests() > tableEntity.getNumberOfSeats()) {
            throw new RuntimeException("Guests exceed seats");
        }

        LocalDateTime endTime = request.reservationTime().plus(RESERVATION_DURATION);
        List<Reservation> overlapping = reservationRepository.findOverlapping(request.tableId(), request.reservationTime(), endTime);
        if (!overlapping.isEmpty()) {
            throw new RuntimeException("Overlapping reservation");
        }

        Reservation reservation = new Reservation();
        reservation.setClient(clientEntity);
        reservation.setTable(tableEntity);
        reservation.setReservationTime(request.reservationTime());
        reservation.setNumberOfGuests(request.numberOfGuests());
        reservation.setStatus("PENDING");

        // Сначала сохраняем, чтобы получить ID
        reservation = reservationRepository.create(reservation);

        try {
            PriceRequest priceRequest = PriceRequest.newBuilder()
                    .setReservationId(reservation.getId())
                    .setNumberOfGuests(request.numberOfGuests())
                    .setTableType(tableEntity.getType() != null ? tableEntity.getType() : "STANDARD")
                    .setDurationHours(2)  // Фикс.
                    .build();
            PriceResponse priceResponse = priceStub.calculatePrice(priceRequest);
            reservation.setPrice(priceResponse.getPrice());

            // Публикация события в Fanout
            ReservationPricedEvent pricedEvent = new ReservationPricedEvent(
                    reservation.getId(),
                    clientEntity.getId(),
                    tableEntity.getId(),
                    priceResponse.getPrice(),
                    priceResponse.getVerdict()
            );
            rabbitTemplate.convertAndSend(RabbitMQConfig.FANOUT_EXCHANGE, "", pricedEvent);  // "" - routingKey игнорируется
            
            // Обновляем резервацию с ценой
            reservation = reservationRepository.update(reservation);
        } catch (StatusRuntimeException e) {
            // Отказоустойчивость (№9)
            reservation.setPrice(-1.0);
            log.error("gRPC error: {}", e.getStatus());
            reservation = reservationRepository.update(reservation);
        }


        // Публикуем событие после успешного создания
        ReservationCreatedEvent event = new ReservationCreatedEvent(
                reservation.getId(),
                clientEntity.getId(),
                clientEntity.getName(),
                tableEntity.getId(),
                tableEntity.getNumber(),
                reservation.getReservationTime(),
                reservation.getNumberOfGuests()
        );

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                RabbitMQConfig.ROUTING_KEY_RESERVATION_CREATED,
                event
        );



        return toResponse(reservation);
    }

    public ReservationResponse update(Long id, UpdateReservationRequest request) {
        Reservation reservation = reservationRepository.findById(id);
        if (reservation == null) {
            throw new ResourceNotFoundException("Reservation", id);
        }

        TableEntity tableEntity = reservation.getTable();

        if (request.numberOfGuests() > tableEntity.getNumberOfSeats()) {
            throw new RuntimeException("Guests exceed seats");
        }

        LocalDateTime endTime = request.reservationTime().plus(RESERVATION_DURATION);
        List<Reservation> overlapping = reservationRepository.findOverlapping(tableEntity.getId(), request.reservationTime(), endTime);
        overlapping.removeIf(r -> r.getId().equals(id)); // Исключаем само бронирование
        if (!overlapping.isEmpty()) {
            throw new RuntimeException("Overlapping reservation");
        }

        reservation.setReservationTime(request.reservationTime());
        reservation.setNumberOfGuests(request.numberOfGuests());


        try {
            PriceRequest priceRequest = PriceRequest.newBuilder()
                    .setReservationId(id)
                    .setNumberOfGuests(request.numberOfGuests())
                    .setTableType(tableEntity.getType() != null ? tableEntity.getType() : "STANDARD")
                    .setDurationHours(2)
                    .build();
            PriceResponse priceResponse = priceStub.calculatePrice(priceRequest);
            reservation.setPrice(priceResponse.getPrice());

            // Публикация события
            ReservationPricedEvent pricedEvent = new ReservationPricedEvent(
                    id,
                    reservation.getClient().getId(),
                    tableEntity.getId(),
                    priceResponse.getPrice(),
                    priceResponse.getVerdict()
            );
            rabbitTemplate.convertAndSend(RabbitMQConfig.FANOUT_EXCHANGE, "", pricedEvent);
        } catch (StatusRuntimeException e) {
            reservation.setPrice(-1.0);
            log.error("gRPC error: {}", e.getStatus());
        }







        reservation = reservationRepository.update(reservation);
        return toResponse(reservation);
    }

    public void delete(Long id) {
        Reservation reservation = reservationRepository.findById(id);
        if (reservation == null) {
            throw new ResourceNotFoundException("Reservation", id);
        }

// Сохраняем ID перед удалением
        Long reservationId = reservation.getId();

        reservationRepository.delete(id);


// Публикуем событие после успешного удаления
        ReservationDeletedEvent event = new ReservationDeletedEvent(reservationId);

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                RabbitMQConfig.ROUTING_KEY_RESERVATION_DELETED,
                event
        );


    }

    private ReservationResponse toResponse(Reservation reservation) {
        Client clientEntity = reservation.getClient();
        TableEntity tableEntity = reservation.getTable();

        ClientResponse client = new ClientResponse(clientEntity.getId(), clientEntity.getName(), clientEntity.getEmail(), clientEntity.getPhone());
        TableResponse table = new TableResponse(tableEntity.getId(), tableEntity.getNumber(), tableEntity.getNumberOfSeats(), tableEntity.getType(), tableEntity.isAvailable());

        return new ReservationResponse(reservation.getId(), client, table, reservation.getReservationTime(), reservation.getNumberOfGuests(), reservation.getStatus(), reservation.getPrice());
    }


    @GrpcClient("reservation-price-service")
    private ReservationPriceServiceGrpc.ReservationPriceServiceBlockingStub priceStub;

}