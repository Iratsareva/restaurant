# Изменения: Добавление мониторинга, трассировки и Docker

## Выполненные задачи

### 1. Добавлены зависимости во все сервисы

Во все 4 сервиса добавлены следующие зависимости в `pom.xml`:
- `spring-boot-starter-actuator` - для мониторинга
- `micrometer-registry-prometheus` - экспорт метрик в Prometheus
- `micrometer-tracing-bridge-brave` - трассировка запросов
- `zipkin-reporter-brave` - отправка трейсов в Zipkin
- `logstash-logback-encoder` - JSON логирование

**Сервисы:**
- ✅ demo (demo-rest)
- ✅ reservation-price-service
- ✅ notification-service
- ✅ audit-service

### 2. Создан logback-spring.xml для всех сервисов

Файл `src/main/resources/logback-spring.xml` создан во всех сервисах для JSON логирования с поддержкой traceId и spanId.

### 3. Обновлены application.properties

Добавлены настройки Actuator и Zipkin во все сервисы:
```properties
management.endpoints.web.exposure.include=*
management.endpoint.health.show-details=always
management.tracing.sampling.probability=1.0
management.zipkin.tracing.endpoint=http://zipkin:9411/api/v2/spans
management.metrics.export.prometheus.enabled=true
```

### 4. Отключен самописный LoggingAndTracingFilter

В `demo/src/main/java/com/example/demo/filters/LoggingAndTracingFilter.java` закомментирована аннотация `@Component`, так как теперь используется Micrometer Tracing.

### 5. Созданы Dockerfile для всех сервисов

Созданы Dockerfile в следующих местах:
- ✅ `demo/Dockerfile` (порт 8080)
- ✅ `reservation-price-service/Dockerfile` (порт 9090)
- ✅ `notification-service/notification-service/Dockerfile` (порт 8083)
- ✅ `audit-service/audit-service/Dockerfile` (порт 8082)

### 6. Создан docker-compose.yml

Создан файл `docker-compose.yml` в корне проекта с конфигурацией:
- RabbitMQ (порты 5672, 15672)
- Zipkin (порт 9411)
- Prometheus (порт 9091, внутренний 9090)
- PostgreSQL (порт 5433)
- Все 4 микросервиса

### 7. Создан prometheus.yml

Создан файл `prometheus.yml` для сбора метрик со всех сервисов.

### 8. Обновлены зависимости events-contract

- Обновлен `groupId` в `events-contract/pom.xml` на `org.example.restaurant`
- Убраны `system` scope зависимости во всех сервисах
- Теперь используется стандартный Maven репозиторий

## Следующие шаги

1. **Установить events-contract в локальный Maven репозиторий:**
   ```bash
   cd events-contract
   mvn clean install
   ```

2. **Собрать все сервисы:**
   ```bash
   cd demo && mvn clean package -DskipTests
   cd ../reservation-price-service && mvn clean package -DskipTests
   cd ../notification-service/notification-service && mvn clean package -DskipTests
   cd ../../audit-service/audit-service && mvn clean package -DskipTests
   ```

3. **Запустить через Docker Compose:**
   ```bash
   docker-compose up --build -d
   ```

4. **Проверить работу:**
   - Zipkin: http://localhost:9411
   - Prometheus: http://localhost:9091
   - Actuator: http://localhost:8080/actuator/health

## Примечания

- Для локальной разработки используйте `localhost` в application.properties
- В Docker окружении сервисы обращаются друг к другу по именам из docker-compose.yml
- Все логи теперь в формате JSON с traceId и spanId
- Трассировка автоматически прокидывается через HTTP, RabbitMQ и gRPC

