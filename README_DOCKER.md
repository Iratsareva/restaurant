# Инструкция по запуску проекта в Docker

## Предварительные требования

1. Установленный Docker и Docker Compose
2. Maven установлен в системе

## ⚠️ Важно: Установка зависимостей (обязательно на новом устройстве)

**Перед запуском проекта на новом устройстве выполните установку локальных зависимостей:**

Смотрите подробную инструкцию: [DEPENDENCIES_SETUP.md](DEPENDENCIES_SETUP.md)

### Быстрая установка:
- **Windows**: `install-dependencies.cmd`
- **Linux/Mac**: `./install-dependencies.sh`

## Шаг 1: Запуск всех сервисов

После установки зависимостей запустите проект:

### Для Linux/Mac:
```bash
cd demo
mvn clean package -DskipTests

cd ../reservation-price-service
mvn clean package -DskipTests

cd ../notification-service/notification-service
mvn clean package -DskipTests

cd ../../audit-service/audit-service
mvn clean package -DskipTests
```

### Для Windows:
```bash
cd demo
mvnw.cmd clean package -DskipTests

cd ..\reservation-price-service
mvnw.cmd clean package -DskipTests

cd ..\notification-service\notification-service
mvnw.cmd clean package -DskipTests

cd ..\..\audit-service\audit-service
mvnw.cmd clean package -DskipTests
```

## Шаг 3: Запуск через Docker Compose

В корневой папке проекта выполните:

```bash
docker-compose up --build -d
```

Флаг `--build` пересоберет все Docker образы, `-d` запустит в фоновом режиме.

## Шаг 4: Просмотр логов

```bash
docker-compose logs -f
```

Или для конкретного сервиса:
```bash
docker-compose logs -f demo-rest
```

## Шаг 5: Проверка работы

### Проверка трассировки (Zipkin):
Откройте в браузере: http://localhost:9411

1. Нажмите "Run Query" для просмотра всех трейсов
2. Сделайте POST запрос к API (например, создание бронирования)
3. Обновите страницу Zipkin - должны появиться новые трейсы

### Проверка метрик (Prometheus):
Откройте в браузере: http://localhost:9091

1. В поиске введите: `http_server_requests_seconds_count`
2. Нажмите "Execute"
3. Вы увидите количество запросов к каждому сервису

### Проверка Actuator endpoints:
- Health: http://localhost:8080/actuator/health
- Metrics: http://localhost:8080/actuator/metrics
- Prometheus: http://localhost:8080/actuator/prometheus

## Остановка сервисов

```bash
docker-compose down
```

Для полной очистки (включая volumes):
```bash
docker-compose down -v
```

## Структура сервисов

- **demo-rest** (порт 8080) - основной REST API
- **reservation-price-service** (порт 9090) - gRPC сервис расчета цены
- **notification-service** (порт 8083) - WebSocket уведомления
- **audit-service** (порт 8082) - сервис аудита
- **rabbitmq** (порты 5672, 15672) - брокер сообщений
- **zipkin** (порт 9411) - трассировка
- **prometheus** (порт 9090) - метрики
- **postgres** (порт 5433) - база данных

## Примечания

1. Все сервисы используют единую Docker сеть `microservices-net`
2. Внутри Docker сети сервисы обращаются друг к другу по именам (например, `rabbitmq`, `zipkin`)
3. Для локальной разработки используйте `localhost` в application.properties
4. Для Docker используйте имена сервисов из docker-compose.yml

