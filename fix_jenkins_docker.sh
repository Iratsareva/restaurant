#!/bin/bash

echo "Исправление Jenkins Docker-in-Docker проблемы..."
echo

echo "Шаг 1: Останавливаем все сервисы"
docker compose down
echo

echo "Шаг 2: Удаляем Jenkins volumes (если существуют)"
docker volume rm restaurant_jenkins_home 2>/dev/null && echo "Volume restaurant_jenkins_home удален"
docker volume rm restaurant_jenkins_docker_data 2>/dev/null && echo "Volume restaurant_jenkins_docker_data удален"
echo

echo "Шаг 3: Пересобираем Jenkins образ (--no-cache, используем Jenkins + Docker CLI)"
docker compose build --no-cache jenkins
echo

echo "Шаг 4: Запускаем Jenkins"
docker compose up -d jenkins
echo

echo "Шаг 5: Ждем запуска Jenkins (60 секунд)..."
sleep 60
echo

echo "Шаг 6: Проверяем статус Jenkins"
docker compose ps jenkins
echo

echo "Шаг 7: Проверяем что Jenkins запустился"
if docker compose exec jenkins ps aux | grep -q java; then
    echo "УСПЕХ: Jenkins процесс запущен"
else
    echo "ОШИБКА: Jenkins не запустился"
    echo "Проверьте логи: docker compose logs jenkins"
fi
echo

echo "Шаг 8: Проверяем Docker в Jenkins контейнере"
if docker compose exec jenkins docker --version >/dev/null 2>&1; then
    echo "УСПЕХ: Docker доступен в Jenkins"
else
    echo "ОШИБКА: Docker не доступен в Jenkins"
    echo "Проверьте логи: docker compose logs jenkins"
fi
echo

echo "Готово! Теперь откройте http://localhost:8088"
echo "И создайте Pipeline с вашим Git репозиторием"
