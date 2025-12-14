package com.example.Restaurant.dto;

import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import java.util.Objects;

@Relation(collectionRelation = "tables", itemRelation = "table")
public class TableResponse extends RepresentationModel<TableResponse> {
    private final Long id;

    private final String number;
    private final int numberOfSeats;
    private final String type;
    private final boolean isAvailable;

    public TableResponse(Long id, String number, int numberOfSeats, String type, boolean isAvailable) {
        this.id = id;
        this.number = number;
        this.numberOfSeats = numberOfSeats;
        this.type = type;
        this.isAvailable = isAvailable;
    }

    public Long getId() {
        return id;
    }
    public String getNumber() {
        return number;
    }
    public int getNumberOfSeats() {
        return numberOfSeats;
    }
    public boolean isAvailable() {
        return isAvailable;
    }

    public String getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        TableResponse that = (TableResponse) o;
        return number.equals(that.number)
                && numberOfSeats == that.numberOfSeats
                && type.equals(type)
                && isAvailable == that.isAvailable
                && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), id, number, numberOfSeats, type,  isAvailable);
    }
}