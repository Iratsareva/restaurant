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



