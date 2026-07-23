package org.example.reservationprice;


import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

/**
 * Реализация gRPC сервиса для расчета цены бронирования.
 * 
 * <p><b>Назначение:</b> Этот класс реализует gRPC сервис, который рассчитывает цену
 * бронирования на основе количества гостей, типа столика и продолжительности.
 * 
 * <p><b>Архитектурная роль:</b>
 * <ul>
 *   <li><b>Microservice</b> - отдельный микросервис для расчета цены</li>
 *   <li><b>gRPC Server</b> - сервер, обрабатывающий gRPC запросы</li>
 *   <li><b>Business Logic</b> - бизнес-логика расчета цены</li>
 * </ul>
 * 
 * <p><b>gRPC Server:</b> Наследуется от сгенерированного класса ReservationPriceServiceGrpc.ReservationPriceServiceImplBase,
 * который создается автоматически из .proto файла при компиляции проекта.
 * 
 * <p><b>Аннотация @GrpcService:</b> Помечает класс как gRPC сервис. Spring Boot автоматически
 * регистрирует его и делает доступным по gRPC протоколу.
 * 
 * <p><b>Формула расчета цены:</b>
 * <pre>
 * price = baseRate × numberOfGuests × durationHours
 * 
 * где:
 * - baseRate = 150.0 для VIP столиков, 100.0 для остальных
 * - numberOfGuests = количество гостей
 * - durationHours = продолжительность (обычно 2 часа)
 * </pre>
 * 
 * <p><b>Вердикт:</b> Определяется на основе порогового значения 1000 рублей:
 * <ul>
 *   <li>Если price > 1000 → "EXPENSIVE" (дорого)</li>
 *   <li>Иначе → "AFFORDABLE" (доступно)</li>
 * </ul>
 * 
 * <p><b>StreamObserver:</b> Используется для асинхронной отправки ответа клиенту.
 * <ul>
 *   <li>onNext() - отправка ответа</li>
 *   <li>onCompleted() - завершение потока</li>
 *   <li>onError() - отправка ошибки</li>
 * </ul>
 * 
 * <p><b>Пример расчета:</b>
 * <pre>
 * VIP столик, 4 гостя, 2 часа:
 * price = 150.0 × 4 × 2 = 1200 рублей
 * verdict = "EXPENSIVE" (1200 > 1000)
 * </pre>
 * 
 * @author Restaurant System
 * @version 1.0
 * @see ReservationPriceServiceGrpc
 * @see PriceRequest
 * @see PriceResponse
 */
@GrpcService
public class ReservationPriceServiceImpl extends ReservationPriceServiceGrpc.ReservationPriceServiceImplBase {

    /**
     * Рассчитать цену бронирования.
     * 
     * <p><b>Алгоритм:</b>
     * <ol>
     *   <li>Определение базовой ставки на основе типа столика (VIP = 150, остальные = 100)</li>
     *   <li>Расчет цены: baseRate × numberOfGuests × durationHours</li>
     *   <li>Определение вердикта: EXPENSIVE если > 1000, иначе AFFORDABLE</li>
     *   <li>Создание ответа через Builder паттерн</li>
     *   <li>Отправка ответа клиенту</li>
     * </ol>
     * 
     * <p><b>Builder Pattern:</b> Protocol Buffers использует Builder паттерн для создания
     * неизменяемых объектов. Это обеспечивает типобезопасность и валидацию полей.
     * 
     * @param request Запрос с параметрами бронирования
     * @param responseObserver Наблюдатель для отправки ответа клиенту
     */
    @Override
    public void calculatePrice(PriceRequest request, StreamObserver<PriceResponse> responseObserver) {
        // Определение базовой ставки: VIP столики дороже
        double baseRate = "VIP".equalsIgnoreCase(request.getTableType()) ? 150.0 : 100.0;
        
        // Расчет цены: базовая ставка × количество гостей × продолжительность
        double price = baseRate * request.getNumberOfGuests() * request.getDurationHours();
        
        // Определение вердикта на основе порогового значения
        String verdict = price > 1000 ? "EXPENSIVE" : "AFFORDABLE";

        // Создание ответа через Builder (неизменяемый объект)
        PriceResponse response = PriceResponse.newBuilder()
                .setReservationId(request.getReservationId())
                .setPrice(price)
                .setVerdict(verdict)
                .build();

        // Отправка ответа клиенту
        responseObserver.onNext(response);
        // Завершение потока
        responseObserver.onCompleted();
    }

    /**
     * Обновить цену бронирования при изменении количества гостей.
     * 
     * <p><b>Алгоритм:</b>
     * <ol>
     *   <li>Валидация reservation_id (должен быть > 0)</li>
     *   <li>Расчет новой цены с фиксированной базовой ставкой 100.0</li>
     *   <li>Определение вердикта</li>
     *   <li>Отправка обновленной цены</li>
     * </ol>
     * 
     * <p><b>Обработка ошибок:</b> Если reservation_id невалиден, отправляется
     * gRPC ошибка со статусом NOT_FOUND.
     * 
     * @param request Запрос с новым количеством гостей
     * @param responseObserver Наблюдатель для отправки ответа
     */
    @Override
    public void updatePrice(UpdatePriceRequest request, StreamObserver<PriceResponse> responseObserver) {
        // Валидация: reservation_id должен быть положительным
        if (request.getReservationId() <= 0) {
            // Отправка gRPC ошибки клиенту
            responseObserver.onError(Status.NOT_FOUND.withDescription("Reservation not found").asRuntimeException());
            return;
        }
        
        // Фиксированная базовая ставка для обновления (не учитывается тип столика)
        double baseRate = 100.0;
        // Расчет новой цены: базовая ставка × новое количество гостей × 2 часа
        double newPrice = baseRate * request.getNewNumberOfGuests() * 2;
        // Определение вердикта
        String verdict = newPrice > 1000 ? "EXPENSIVE" : "AFFORDABLE";

        // Создание и отправка ответа
        PriceResponse response = PriceResponse.newBuilder()
                .setReservationId(request.getReservationId())
                .setPrice(newPrice)
                .setVerdict(verdict)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
