package org.example.reservationprice;


import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
public class ReservationPriceServiceImpl extends ReservationPriceServiceGrpc.ReservationPriceServiceImplBase {

    @Override
    public void calculatePrice(PriceRequest request, StreamObserver<PriceResponse> responseObserver) {
        // Простая формула: базовая ставка * гости * часы
        double baseRate = "VIP".equalsIgnoreCase(request.getTableType()) ? 150.0 : 100.0;
        double price = baseRate * request.getNumberOfGuests() * request.getDurationHours();
        String verdict = price > 1000 ? "EXPENSIVE" : "AFFORDABLE";

        PriceResponse response = PriceResponse.newBuilder()
                .setReservationId(request.getReservationId())
                .setPrice(price)
                .setVerdict(verdict)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void updatePrice(UpdatePriceRequest request, StreamObserver<PriceResponse> responseObserver) {
        // Симуляция проверки существования (из задания №8: обработка "не найден")
        if (request.getReservationId() <= 0) {
            responseObserver.onError(Status.NOT_FOUND.withDescription("Reservation not found").asRuntimeException());
            return;
        }

        // Перерасчет цены (используем фикс. значения для демонстрации)
        double baseRate = 100.0;  // Упрощенно
        double newPrice = baseRate * request.getNewNumberOfGuests() * 2;  // Фикс. 2 часа
        String verdict = newPrice > 1000 ? "EXPENSIVE" : "AFFORDABLE";

        PriceResponse response = PriceResponse.newBuilder()
                .setReservationId(request.getReservationId())
                .setPrice(newPrice)
                .setVerdict(verdict)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
