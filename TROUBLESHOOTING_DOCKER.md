# Решение проблем с Docker Compose

## 🔍 Диагностика проблем

### Шаг 1: Проверьте статус контейнеров

```bash
docker-compose ps
```

**Что смотреть:**
- **Status**: `Up` - контейнер работает, `Exited` - контейнер остановился, `Restarting` - контейнер перезапускается
- **Exit Code**: `0` - нормальное завершение, другие коды - ошибка

### Шаг 2: Просмотрите логи

```bash
# Логи всех контейнеров
docker-compose logs

# Логи конкретного контейнера
docker-compose logs <service-name>

# Логи последних 100 строк
docker-compose logs --tail=100

# Логи в реальном времени
docker-compose logs -f
```

**Что искать в логах:**
- Ошибки подключения к БД
- Ошибки подключения к RabbitMQ
- Ошибки портов (Address already in use)
- Ошибки зависимостей
- Ошибки сборки

---

## 🛠️ Частые проблемы и решения

### Проблема 1: Порт уже занят

**Симптомы:**
```
Error: bind: address already in use
Error: port is already allocated
```

**Решение:**

1. **Найдите процесс, занимающий порт:**
   ```bash
   # Windows
   netstat -ano | findstr :8080
   
   # Linux/Mac
   lsof -i :8080
   ```

2. **Остановите процесс или измените порт:**
   
   **Вариант A: Остановите процесс**
   ```bash
   # Windows - найдите PID и остановите
   taskkill /PID <PID> /F
   
   # Linux/Mac
   kill -9 <PID>
   ```
   
   **Вариант B: Измените порт в docker-compose.yml**
   ```yaml
   services:
     jenkins:
       ports:
         - "8082:8080"  # Измените внешний порт
   ```

3. **Остановите все контейнеры:**
   ```bash
   docker-compose down
   ```

4. **Запустите снова:**
   ```bash
   docker-compose up -d
   ```

---

### Проблема 2: Контейнеры постоянно перезапускаются (Restarting)

**Симптомы:**
```
Status: Restarting (1) 2 seconds ago
```

**Решение:**

1. **Проверьте логи:**
   ```bash
   docker-compose logs <service-name>
   ```

2. **Проверьте health checks:**
   ```bash
   docker-compose ps
   # Смотрите на статус health check
   ```

3. **Временно отключите health check для диагностики:**
   В `docker-compose.yml` закомментируйте healthcheck:
   ```yaml
   # healthcheck:
   #   test: ["CMD", "..."]

4. **Проверьте зависимости:**
   Убедитесь, что все зависимости (RabbitMQ, PostgreSQL) запущены:
   ```bash
   docker-compose ps rabbitmq postgres
   ```

---

### Проблема 3: Ошибка подключения к базе данных

**Симптомы:**
```
Connection refused
Unable to connect to database
```

**Решение:**

1. **Проверьте, что PostgreSQL запущен:**
   ```bash
   docker-compose ps postgres
   ```

2. **Проверьте логи PostgreSQL:**
   ```bash
   docker-compose logs postgres
   ```

3. **Проверьте переменные окружения:**
   В `docker-compose.yml` убедитесь, что:
   ```yaml
   postgres:
     environment:
       - POSTGRES_DB=restaurant_db
       - POSTGRES_USER=postgres
       - POSTGRES_PASSWORD=290404
   ```

4. **Дождитесь готовности PostgreSQL:**
   ```bash
   # Проверьте health check
   docker-compose ps postgres
   # Должен быть статус "healthy"
   ```

5. **Пересоздайте volume (если нужно):**
   ```bash
   docker-compose down -v  # Удалит volumes
   docker-compose up -d postgres
   # Подождите 10-15 секунд
   docker-compose up -d
   ```

---

### Проблема 4: Ошибка подключения к RabbitMQ

**Симптомы:**
```
Connection refused to rabbitmq:5672
```

**Решение:**

1. **Проверьте, что RabbitMQ запущен:**
   ```bash
   docker-compose ps rabbitmq
   ```

2. **Дождитесь готовности RabbitMQ:**
   ```bash
   # Проверьте health check
   docker-compose ps rabbitmq
   # Должен быть статус "healthy"
   ```

3. **Проверьте переменные окружения:**
   Убедитесь, что все сервисы используют правильный хост:
   ```yaml
   environment:
     - SPRING_RABBITMQ_HOST=rabbitmq  # Не localhost!
   ```

4. **Проверьте сеть:**
   ```bash
   docker network ls
   docker network inspect <network-name>
   ```

---

### Проблема 5: Ошибка сборки Docker образов

**Симптомы:**
```
ERROR: failed to build
Cannot find JAR file
```

**Решение:**

1. **Соберите проекты перед сборкой образов:**
   ```bash
   # Соберите events-contract
   cd events-contract
   mvn clean install
   
   # Соберите все сервисы
   cd ../demo && mvn clean package -DskipTests
   cd ../reservation-price-service && mvn clean package -DskipTests
   cd ../notification-service/notification-service && mvn clean package -DskipTests
   cd ../../audit-service/audit-service && mvn clean package -DskipTests
   cd ../..
   ```

2. **Пересоберите образы:**
   ```bash
   docker-compose build --no-cache
   ```

3. **Проверьте Dockerfile:**
   Убедитесь, что путь к JAR правильный:
   ```dockerfile
   ARG JAR_FILE=target/*.jar
   COPY ${JAR_FILE} app.jar
   ```

---

### Проблема 6: Недостаточно памяти

**Симптомы:**
```
Out of memory
Cannot allocate memory
```

**Решение:**

1. **Увеличьте память Docker:**
   - Docker Desktop: Settings → Resources → Memory
   - Установите минимум 4GB (рекомендуется 8GB)

2. **Остановите ненужные контейнеры:**
   ```bash
   docker ps -a
   docker stop <container-id>
   docker rm <container-id>
   ```

3. **Очистите неиспользуемые ресурсы:**
   ```bash
   docker system prune -a
   ```

---

### Проблема 7: Контейнеры не видят друг друга

**Симптомы:**
```
Name resolution failed
Cannot resolve hostname
```

**Решение:**

1. **Проверьте сеть:**
   ```bash
   docker network ls
   docker network inspect restaurant-project_microservices-net
   ```

2. **Убедитесь, что все сервисы в одной сети:**
   В `docker-compose.yml` все сервисы должны иметь:
   ```yaml
   networks:
     - microservices-net
   ```

3. **Используйте имена сервисов, а не localhost:**
   ```yaml
   # Правильно
   SPRING_RABBITMQ_HOST=rabbitmq
   
   # Неправильно
   SPRING_RABBITMQ_HOST=localhost
   ```

---

## 🔧 Пошаговая диагностика

### Полная диагностика проблемы

```bash
# 1. Остановите все контейнеры
docker-compose down

# 2. Проверьте, что порты свободны
# Windows
netstat -ano | findstr ":8080 :8081 :8082 :8083 :9090 :9091 :3000 :9411 :5672 :15672"

# Linux/Mac
lsof -i :8080 -i :8081 -i :8082 -i :8083 -i :9090 -i :9091 -i :3000 -i :9411 -i :5672 -i :15672

# 3. Очистите старые контейнеры и образы
docker-compose down -v
docker system prune -f

# 4. Соберите проекты
cd events-contract && mvn clean install && cd ..
cd demo && mvn clean package -DskipTests && cd ..
cd reservation-price-service && mvn clean package -DskipTests && cd ..
cd notification-service/notification-service && mvn clean package -DskipTests && cd ../..
cd audit-service/audit-service && mvn clean package -DskipTests && cd ../..

# 5. Запустите инфраструктуру сначала
docker-compose up -d rabbitmq postgres

# 6. Подождите 30 секунд
sleep 30

# 7. Проверьте статус инфраструктуры
docker-compose ps rabbitmq postgres

# 8. Запустите остальные сервисы
docker-compose up -d

# 9. Проверьте логи
docker-compose logs --tail=50
```

---

## 📋 Чеклист решения проблем

- [ ] Проверены логи всех контейнеров
- [ ] Проверены занятые порты
- [ ] Проверена доступность Docker ресурсов (память, диск)
- [ ] Проверены переменные окружения
- [ ] Проверена сеть Docker
- [ ] Проверены зависимости между сервисами
- [ ] Проекты собраны перед сборкой образов
- [ ] Health checks работают корректно
- [ ] Все сервисы в одной сети

---

## 🚀 Быстрое решение (если ничего не помогает)

```bash
# 1. Полная очистка
docker-compose down -v
docker system prune -a -f

# 2. Пересборка проектов
cd events-contract && mvn clean install && cd ..
cd demo && mvn clean package -DskipTests && cd ..
cd reservation-price-service && mvn clean package -DskipTests && cd ..
cd notification-service/notification-service && mvn clean package -DskipTests && cd ../..
cd audit-service/audit-service && mvn clean package -DskipTests && cd ../..

# 3. Пересборка образов
docker-compose build --no-cache

# 4. Запуск с просмотром логов
docker-compose up
# (без -d, чтобы видеть логи в реальном времени)
```

---

## 💡 Полезные команды

```bash
# Просмотр всех контейнеров
docker ps -a

# Просмотр логов конкретного сервиса
docker-compose logs -f <service-name>

# Перезапуск конкретного сервиса
docker-compose restart <service-name>

# Выполнение команды в контейнере
docker-compose exec <service-name> sh

# Просмотр использования ресурсов
docker stats

# Очистка всего
docker-compose down -v
docker system prune -a --volumes
```

---

## 📞 Если проблема не решена

1. **Соберите информацию:**
   ```bash
   # Логи всех сервисов
   docker-compose logs > logs.txt
   
   # Статус контейнеров
   docker-compose ps > status.txt
   
   # Информация о системе
   docker info > docker-info.txt
   ```

2. **Проверьте:**
   - Версию Docker и Docker Compose
   - Достаточно ли памяти
   - Свободно ли место на диске
   - Нет ли конфликтов портов

3. **Попробуйте запустить сервисы по одному:**
   ```bash
   # Сначала инфраструктура
   docker-compose up -d rabbitmq postgres
   sleep 30
   
   # Потом мониторинг
   docker-compose up -d prometheus grafana zipkin
   sleep 10
   
   # Потом сервисы
   docker-compose up -d
   ```



