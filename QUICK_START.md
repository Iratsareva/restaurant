# Быстрый старт проекта

## 🚀 Быстрый запуск через Docker (рекомендуется)

### Для новых устройств (одноразовая настройка)
Если вы запускаете проект на **новом устройстве** впервые:

```bash
# Windows
install-dependencies.cmd

# Linux/Mac
chmod +x install-dependencies.sh && ./install-dependencies.sh
```

### Обычный запуск

```bash
# В корне проекта - одна команда запускает всё
docker compose up -d --build
```

### 🔍 Что происходит при сборке
- Docker автоматически собирает все контракты (`restaurant_api_contracts`, `events-contract`)
- Устанавливает их в локальный Maven репозиторий внутри контейнера
- Собирает все микросервисы
- Запускает всю инфраструктуру (PostgreSQL, RabbitMQ, Redis, Grafana, Prometheus, Jenkins)

---

## 🔧 Ручной локальный запуск (без Docker)

### 1. Предварительная подготовка

```bash
# Установите events-contract в локальный Maven репозиторий
cd events-contract
mvn clean install
```

### 2. Сборка всех сервисов

```bash
# Вернитесь в корень проекта
cd ..

# Соберите все сервисы
cd demo && mvn clean package -DskipTests && cd ..
cd reservation-price-service && mvn clean package -DskipTests && cd ..
cd notification-service/notification-service && mvn clean package -DskipTests && cd ../..
cd audit-service/audit-service && mvn clean package -DskipTests && cd ../..
```

### 3. Запуск через Docker Compose

```bash
# В корне проекта
docker-compose up --build -d
```

### 4. Проверка работы

Откройте в браузере:

- **Demo REST API**: http://localhost:8081 (порт изменен, чтобы не конфликтовать с Jenkins)
- **Prometheus**: http://localhost:9091
- **Grafana**: http://localhost:3000 (логин: `admin`, пароль: `admin`)
- **Zipkin**: http://localhost:9411
- **RabbitMQ Management**: http://localhost:15672 (логин: `guest`, пароль: `guest`)

### 5. Проверка метрик

1. **В Prometheus:**
   - Откройте http://localhost:9091
   - В поиске введите: `http_server_requests_seconds_count`
   - Нажмите Execute

2. **В Grafana:**
   - Откройте http://localhost:3000
   - Войдите (admin/admin)
   - Дашборд "Restaurant Microservices Metrics" должен быть доступен автоматически

### 6. Тестирование API

```bash
# Создайте бронирование
curl -X POST http://localhost:8080/api/reservations \
  -H "Content-Type: application/json" \
  -d '{
    "clientId": 1,
    "tableId": 1,
    "reservationTime": "2025-12-20T19:00:00",
    "numberOfGuests": 4
  }'
```

После этого проверьте:
- Метрики в Prometheus обновились
- Трейсы появились в Zipkin
- Уведомления пришли в notification-service (если открыт WebSocket клиент)

## 🔄 Запуск через Jenkins

### 1. Настройка Jenkins

Следуйте инструкциям в `JENKINS_SETUP.md`

### 2. Создание Pipeline Job

1. В Jenkins создайте новый Pipeline job
2. Укажите путь к `Jenkinsfile` в репозитории
3. Настройте подключение к Git репозиторию

### 3. Запуск Pipeline

1. Нажмите "Build Now"
2. Следите за выполнением в Console Output
3. После успешного выполнения все сервисы будут запущены

## 📊 Просмотр метрик

### Prometheus Queries

Полезные запросы для Prometheus:

```promql
# Количество HTTP запросов
sum(rate(http_server_requests_seconds_count[5m])) by (instance)

# Время ответа (95 перцентиль)
histogram_quantile(0.95, sum(rate(http_server_requests_seconds_bucket[5m])) by (le, instance))

# Использование памяти JVM
sum(jvm_memory_used_bytes) by (instance)

# Активные соединения к БД
sum(jdbc_connections_active) by (instance)
```

### Grafana Dashboard

Дашборд включает:
- HTTP Requests Total - общее количество запросов
- HTTP Request Duration - время ответа
- JVM Memory Usage - использование памяти
- Active Connections - активные соединения
- RabbitMQ Messages - сообщения в очереди

## 🛑 Остановка сервисов

```bash
docker-compose down
```

Для полной очистки (включая volumes):

```bash
docker-compose down -v
```

## 🔧 Troubleshooting

### Порт уже занят

```bash
# Проверьте, какие порты заняты
netstat -ano | findstr :8080  # Windows
lsof -i :8080                  # Linux/Mac

# Остановите контейнеры
docker-compose down
```

### Ошибка сборки Maven

```bash
# Убедитесь, что events-contract установлен
cd events-contract
mvn clean install

# Очистите кэш Maven
mvn dependency:purge-local-repository
```

### Сервисы не запускаются

```bash
# Проверьте логи
docker-compose logs -f <service-name>

# Пересоберите образы
docker-compose up --build --force-recreate
```

## 📝 Полезные команды

```bash
# Просмотр логов всех сервисов
docker-compose logs -f

# Просмотр логов конкретного сервиса
docker-compose logs -f demo-rest

# Перезапуск конкретного сервиса
docker-compose restart demo-rest

# Просмотр статуса сервисов
docker-compose ps

# Выполнение команды в контейнере
docker-compose exec demo-rest sh
```

