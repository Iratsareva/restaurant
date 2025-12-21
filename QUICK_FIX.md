# 🚨 Быстрое решение проблем с Docker Compose

## Если контейнеры не запускаются - выполните это:

### Вариант 1: Быстрая перезагрузка (2 минуты)

```bash
# 1. Остановите все
docker-compose down

# 2. Запустите снова
docker-compose up -d

# 3. Проверьте логи
docker-compose logs --tail=50
```

### Вариант 2: Полная перезагрузка (5 минут)

```bash
# 1. Остановите и удалите все
docker-compose down -v

# 2. Очистите Docker
docker system prune -f

# 3. Запустите инфраструктуру сначала
docker-compose up -d rabbitmq postgres

# 4. Подождите 30 секунд
# (в Windows используйте: timeout /t 30)

# 5. Запустите все остальное
docker-compose up -d

# 6. Проверьте статус
docker-compose ps
```

### Вариант 3: Если порты заняты

```bash
# 1. Найдите процессы на портах
# Windows:
netstat -ano | findstr ":8080 :8081 :8082 :8083"

# Linux/Mac:
lsof -i :8080 -i :8081 -i :8082 -i :8083

# 2. Остановите процессы или измените порты в docker-compose.yml

# 3. Запустите снова
docker-compose up -d
```

### Вариант 4: Если не хватает памяти

```bash
# 1. Остановите все контейнеры
docker-compose down

# 2. Очистите неиспользуемые ресурсы
docker system prune -a -f

# 3. Увеличьте память в Docker Desktop (Settings → Resources)

# 4. Запустите снова
docker-compose up -d
```

---

## 🔍 Диагностика проблемы

### Шаг 1: Проверьте статус

```bash
docker-compose ps
```

**Что смотреть:**
- Если статус `Exited` - контейнер упал
- Если статус `Restarting` - контейнер перезапускается

### Шаг 2: Проверьте логи

```bash
# Все логи
docker-compose logs

# Конкретный сервис
docker-compose logs jenkins
docker-compose logs demo-rest
docker-compose logs postgres
docker-compose logs rabbitmq
```

**Что искать:**
- `Connection refused` - проблема с подключением
- `Address already in use` - порт занят
- `Out of memory` - не хватает памяти
- `Cannot find` - проблема с файлами

### Шаг 3: Проверьте порты

```bash
# Windows
netstat -ano | findstr ":8080"

# Linux/Mac
lsof -i :8080
```

---

## ✅ Частые проблемы и быстрые решения

### Проблема: "Port already in use"

**Решение:**
```bash
# Остановите процесс или измените порт в docker-compose.yml
docker-compose down
# Измените порт в docker-compose.yml, например:
# ports:
#   - "8082:8080"  # Вместо 8080:8080
docker-compose up -d
```

### Проблема: "Cannot connect to database"

**Решение:**
```bash
# Убедитесь, что PostgreSQL запущен
docker-compose up -d postgres
# Подождите 30 секунд
docker-compose up -d
```

### Проблема: "Cannot connect to RabbitMQ"

**Решение:**
```bash
# Убедитесь, что RabbitMQ запущен
docker-compose up -d rabbitmq
# Подождите 30 секунд
docker-compose up -d
```

### Проблема: "JAR file not found"

**Решение:**
```bash
# Соберите проекты сначала
cd demo && mvn clean package -DskipTests && cd ..
cd reservation-price-service && mvn clean package -DskipTests && cd ..
cd notification-service/notification-service && mvn clean package -DskipTests && cd ../..
cd audit-service/audit-service && mvn clean package -DskipTests && cd ../..

# Затем запустите
docker-compose up --build -d
```

---

## 🎯 Пошаговая диагностика

```bash
# 1. Проверьте, что Docker работает
docker ps

# 2. Остановите все
docker-compose down

# 3. Проверьте порты
# Windows:
netstat -ano | findstr ":8080 :8081 :8082 :8083 :9090 :9091 :3000 :9411"

# 4. Запустите инфраструктуру
docker-compose up -d rabbitmq postgres

# 5. Подождите
# Windows: timeout /t 30
# Linux/Mac: sleep 30

# 6. Проверьте статус
docker-compose ps rabbitmq postgres

# 7. Запустите все
docker-compose up -d

# 8. Проверьте логи
docker-compose logs --tail=100
```

---

## 📞 Если ничего не помогает

1. **Соберите информацию:**
   ```bash
   docker-compose logs > all-logs.txt
   docker-compose ps > status.txt
   ```

2. **Попробуйте запустить по одному:**
   ```bash
   # Инфраструктура
   docker-compose up -d rabbitmq postgres prometheus grafana zipkin
   
   # Подождите
   sleep 30
   
   # Сервисы
   docker-compose up -d reservation-price-service
   docker-compose up -d notification-service
   docker-compose up -d audit-service
   docker-compose up -d demo-rest
   docker-compose up -d jenkins
   ```

3. **Проверьте каждый сервис отдельно:**
   ```bash
   docker-compose logs <service-name>
   ```

---

## 💡 Полезные команды

```bash
# Перезапуск конкретного сервиса
docker-compose restart <service-name>

# Просмотр логов в реальном времени
docker-compose logs -f <service-name>

# Выполнение команды в контейнере
docker-compose exec <service-name> sh

# Полная очистка
docker-compose down -v
docker system prune -a -f
```



