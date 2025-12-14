package com.example.demo.repository.Impl;

import com.example.demo.models.Reservation;
import com.example.demo.repository.AbstractRepository;
import com.example.demo.repository.ReservationRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class ReservationRepositoryImpl extends AbstractRepository<Reservation> implements ReservationRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public ReservationRepositoryImpl() {
        super(Reservation.class);
    }

    @Override
    public List<Reservation> findByClientId(Long clientId) {
        TypedQuery<Reservation> query = entityManager.createQuery(
                "SELECT r FROM Reservation r WHERE r.client.id = :clientId", Reservation.class);
        return query.setParameter("clientId", clientId).getResultList();
    }

    @Override
    public List<Reservation> findByTableId(Long tableId) {
        TypedQuery<Reservation> query = entityManager.createQuery(
                "SELECT r FROM Reservation r WHERE r.table.id = :tableId", Reservation.class);
        return query.setParameter("tableId", tableId).getResultList();
    }

    @Override
    public List<Reservation> findByStatus(String status) {
        TypedQuery<Reservation> query = entityManager.createQuery(
                "SELECT r FROM Reservation r WHERE r.status = :status", Reservation.class);
        return query.setParameter("status", status).getResultList();
    }

    @Override
    public List<Reservation> findOverlapping(Long tableId, LocalDateTime start, LocalDateTime end) {
        TypedQuery<Reservation> query = entityManager.createQuery(
                "SELECT r FROM Reservation r " +
                        "WHERE r.table.id = :tableId " +
                        "AND r.reservationTime BETWEEN :start AND :end", Reservation.class);
        return query.setParameter("tableId", tableId)
                .setParameter("start", start)
                .setParameter("end", end)
                .getResultList();
    }


    @Override
    public List<Reservation> findByTableType(String type) {
        TypedQuery<Reservation> query = entityManager.createQuery(
                "SELECT r FROM Reservation r WHERE r.table.type = :type", Reservation.class);
        return query.setParameter("type", type).getResultList();
    }

}

