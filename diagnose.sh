#!/bin/bash

echo "🔍 Диагностика Docker Compose..."
echo ""

# Проверка Docker
echo "1. Проверка Docker..."
if ! command -v docker &> /dev/null; then
    echo "❌ Docker не установлен!"
    exit 1
fi
echo "✅ Docker установлен"

# Проверка Docker Compose
echo "2. Проверка Docker Compose..."
if ! command -v docker-compose &> /dev/null; then
    echo "❌ Docker Compose не установлен!"
    exit 1
fi
echo "✅ Docker Compose установлен"

# Проверка статуса контейнеров
echo ""
echo "3. Статус контейнеров:"
docker-compose ps

# Проверка портов
echo ""
echo "4. Проверка занятых портов:"
if command -v lsof &> /dev/null; then
    lsof -i :8080 -i :8081 -i :8082 -i :8083 -i :9090 -i :9091 -i :3000 -i :9411 -i :5672 -i :15672 2>/dev/null || echo "Все порты свободны"
elif command -v netstat &> /dev/null; then
    netstat -ano | grep -E ":8080|:8081|:8082|:8083|:9090|:9091|:3000|:9411|:5672|:15672" || echo "Все порты свободны"
fi

# Проверка логов
echo ""
echo "5. Последние ошибки в логах:"
docker-compose logs --tail=20 2>&1 | grep -i error || echo "Ошибок не найдено"

# Проверка сети
echo ""
echo "6. Проверка Docker сети:"
docker network ls | grep microservices || echo "Сеть не найдена"

# Проверка volumes
echo ""
echo "7. Проверка volumes:"
docker volume ls | grep restaurant || echo "Volumes не найдены"

echo ""
echo "✅ Диагностика завершена!"
echo ""
echo "Для просмотра полных логов выполните:"
echo "  docker-compose logs"



