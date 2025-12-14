package com.example.demo.graphql;


import com.example.Restaurant.dto.*;
import com.example.demo.service.ReservationService;
import com.netflix.graphql.dgs.*;
import org.springframework.stereotype.Component;

@DgsComponent
public class ReservationDataFetcher {

    private final ReservationService reservationService;

    public ReservationDataFetcher(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    // ---------- QUERIES ----------
    @DgsQuery
    public ReservationResponse reservationById(@InputArgument Long id) {
        return reservationService.findById(id);
    }

    @DgsQuery
    public PagedResponse<ReservationResponse> reservations(
            @InputArgument("clientId") Long clientId,
            @InputArgument("tableId") Long tableId,
            @InputArgument("status") String status,
            @InputArgument("tableType") String tableType,
            @InputArgument("page") Integer page,
            @InputArgument("size") Integer size
    ) {
        // Значения по умолчанию
        if (page == null) page = 0;
        if (size == null) size = 10;

        return reservationService.findAll(clientId, tableId, status, tableType, page, size);
    }


    // ---------- MUTATIONS ----------
    @DgsMutation
    public ReservationResponse createReservation(@InputArgument("input") ReservationRequest input) {
        return reservationService.create(input);
    }

    @DgsMutation
    public ReservationResponse updateReservation(@InputArgument Long id, @InputArgument("input") UpdateReservationRequest input) {
        return reservationService.update(id, input);
    }

    @DgsMutation
    public Long deleteReservation(@InputArgument Long id) {
        reservationService.delete(id);
        return id;
    }
}
