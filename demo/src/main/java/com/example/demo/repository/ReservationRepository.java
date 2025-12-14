package com.example.demo.repository;


import com.example.demo.models.Reservation;

import java.time.LocalDateTime;
import java.util.List;

public interface ReservationRepository {
    Reservation create(Reservation reservation);
    Reservation findById(Long id);
    List<Reservation> findAll();
    List<Reservation> findByClientId(Long clientId);
    List<Reservation> findByTableId(Long tableId);
    List<Reservation> findByStatus(String status);
    Reservation update(Reservation reservation);
    void delete(Long id);
    List<Reservation> findOverlapping(Long tableId, LocalDateTime start, LocalDateTime end);

    List<Reservation> findByTableType(String type);
}

