package com.example.Restaurant.dto;

import java.time.LocalDateTime;


import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;
import java.util.Objects;

@Relation(collectionRelation = "reservations", itemRelation = "reservation")
public class ReservationResponse extends RepresentationModel<ReservationResponse> {
    private final Long id;
    private final ClientResponse client;
    private final TableResponse table;
    private final LocalDateTime reservationTime;
    private final int numberOfGuests;
    private final String status;
    private final double price;


    public ReservationResponse(Long id, ClientResponse client, TableResponse table, LocalDateTime reservationTime, int numberOfGuests, String status, double price) {
        this.id = id;
        this.client = client;
        this.table = table;
        this.reservationTime = reservationTime;
        this.numberOfGuests = numberOfGuests;
        this.status = status;
        this.price = price;
    }

    public Long getId() { return id; }
    public ClientResponse getClient() { return client; }
    public TableResponse getTable() { return table; }
    public LocalDateTime getReservationTime() { return reservationTime; }
    public int getNumberOfGuests() { return numberOfGuests; }
    public String getStatus() { return status; }
    public double getPrice() { return price; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ReservationResponse that = (ReservationResponse) o;
        return numberOfGuests == that.numberOfGuests && Objects.equals(id, that.id) && Objects.equals(client, that.client) &&
                Objects.equals(table, that.table) && Objects.equals(reservationTime, that.reservationTime) && Objects.equals(status, that.status) && Objects.equals(price, that.price);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), id, client, table, reservationTime, numberOfGuests, status, price);
    }
}

