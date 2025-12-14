package com.example.demo.models;



import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tables")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class TableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String number;



    @Column(name = "number_of_seats", nullable = false)
    private int numberOfSeats;

    @Column(name = "type")
    private String type;

    @Column(name = "is_available")
    private boolean isAvailable;
}

