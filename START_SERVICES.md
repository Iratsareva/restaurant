# 🚀 Правильный порядок запуска сервисов

## Проблема: Контейнеры не запускаются

Часто проблема в том, что сервисы запускаются одновременно и не успевают дождаться зависимостей.

## ✅ Решение: Запуск по этапам

### Этап 1: Инфраструктура (30 секунд)

```bash
# Запустите только инфраструктуру
docker-compose up -d rabbitmq postgres

# Подождите, пока они станут healthy
docker-compose ps rabbitmq postgres
# Должны быть статусы: "Up (healthy)"
```

**Проверка:**
```bash
# Проверьте логи
docker-compose logs rabbitmq
docker-compose logs postgres

# Проверьте статус
docker-compose ps rabbitmq postgres
```

### Этап 2: Мониторинг (10 секунд)

```bash
# Запустите мониторинг
docker-compose up -d prometheus grafana zipkin

# Подождите
sleep 10

# Проверьте статус
docker-compose ps prometheus grafana zipkin
```

### Этап 3: Сервисы (по одному)

```bash
# 1. Reservation Price Service (gRPC)
docker-compose up -d reservation-price-service
sleep 5

# 2. Notification Service
docker-compose up -d notification-service
sleep 5

# 3. Audit Service
docker-compose up -d audit-service
sleep 5

# 4. Demo REST (зависит от всех выше)
docker-compose up -d demo-rest
sleep 10

# 5. Jenkins (последним)
docker-compose up -d jenkins
```

### Этап 4: Проверка

```bash
# Проверьте все сервисы
docker-compose ps

# Проверьте логи
docker-compose logs --tail=50

# Проверьте health endpoints
curl http://localhost:8081/actuator/health
curl http://localhost:8082/actuator/health
curl http://localhost:8083/actuator/health
```

---

## 🔧 Автоматический скрипт запуска

### Для Linux/Mac: `start-services.sh`

```bash
#!/bin/bash

echo "🚀 Запуск сервисов по этапам..."

# Этап 1: Инфраструктура
echo "Этап 1: Запуск инфраструктуры..."
docker-compose up -d rabbitmq postgres
echo "Ожидание готовности инфраструктуры (30 секунд)..."
sleep 30

# Проверка
if ! docker-compose ps rabbitmq | grep -q "healthy"; then
    echo "❌ RabbitMQ не готов!"
    exit 1
fi

if ! docker-compose ps postgres | grep -q "healthy"; then
    echo "❌ PostgreSQL не готов!"
    exit 1
fi

echo "✅ Инфраструктура готова"

# Этап 2: Мониторинг
echo "Этап 2: Запуск мониторинга..."
docker-compose up -d prometheus grafana zipkin
sleep 10
echo "✅ Мониторинг запущен"

# Этап 3: Сервисы
echo "Этап 3: Запуск сервисов..."
docker-compose up -d reservation-price-service
sleep 5
docker-compose up -d notification-service
sleep 5
docker-compose up -d audit-service
sleep 5
docker-compose up -d demo-rest
sleep 10
docker-compose up -d jenkins
echo "✅ Все сервисы запущены"

# Итоговая проверка
echo ""
echo "📊 Статус всех сервисов:"
docker-compose ps

echo ""
echo "✅ Запуск завершен!"
```

### Для Windows: `start-services.bat`

```batch
@echo off
echo 🚀 Запуск сервисов по этапам...

REM Этап 1: Инфраструктура
echo Этап 1: Запуск инфраструктуры...
docker-compose up -d rabbitmq postgres
echo Ожидание готовности инфраструктуры (30 секунд)...
timeout /t 30 /nobreak

REM Проверка
docker-compose ps rabbitmq | findstr "healthy" >nul
if errorlevel 1 (
    echo ❌ RabbitMQ не готов!
    exit /b 1
)

docker-compose ps postgres | findstr "healthy" >nul
if errorlevel 1 (
    echo ❌ PostgreSQL не готов!
    exit /b 1
)

echo ✅ Инфраструктура готова

REM Этап 2: Мониторинг
echo Этап 2: Запуск мониторинга...
docker-compose up -d prometheus grafana zipkin
timeout /t 10 /nobreak
echo ✅ Мониторинг запущен

REM Этап 3: Сервисы
echo Этап 3: Запуск сервисов...
docker-compose up -d reservation-price-service
timeout /t 5 /nobreak
docker-compose up -d notification-service
timeout /t 5 /nobreak
docker-compose up -d audit-service
timeout /t 5 /nobreak
docker-compose up -d demo-rest
timeout /t 10 /nobreak
docker-compose up -d jenkins
echo ✅ Все сервисы запущены

REM Итоговая проверка
echo.
echo 📊 Статус всех сервисов:
docker-compose ps

echo.
echo ✅ Запуск завершен!
pause
```

---

## 🐛 Если что-то пошло не так

### Проблема: Сервис не запускается

1. **Проверьте логи:**
   ```bash
   docker-compose logs <service-name>
   ```

2. **Проверьте зависимости:**
   ```bash
   docker-compose ps rabbitmq postgres
   # Должны быть "healthy"
   ```

3. **Перезапустите сервис:**
   ```bash
   docker-compose restart <service-name>
   ```

### Проблема: Все еще не работает

1. **Полная перезагрузка:**
   ```bash
   docker-compose down
   docker-compose up -d rabbitmq postgres
   sleep 30
   docker-compose up -d
   ```

2. **Проверьте диагностику:**
   ```bash
   # Linux/Mac
   bash diagnose.sh
   
   # Windows
   diagnose.bat
   ```

---

## 📋 Чеклист запуска

- [ ] Docker и Docker Compose установлены
- [ ] Порты свободны (8080, 8081, 8082, 8083, 9090, 9091, 3000, 9411, 5672, 15672)
- [ ] Проекты собраны (mvn clean package)
- [ ] Инфраструктура запущена и здорова (rabbitmq, postgres)
- [ ] Мониторинг запущен (prometheus, grafana, zipkin)
- [ ] Сервисы запущены по порядку
- [ ] Все сервисы в статусе "Up"
- [ ] Health checks проходят успешно

---

## 💡 Советы

1. **Всегда запускайте инфраструктуру первой**
2. **Дождитесь health checks** перед запуском сервисов
3. **Проверяйте логи** при проблемах
4. **Используйте скрипты** для автоматизации
5. **Запускайте сервисы по одному** при проблемах



