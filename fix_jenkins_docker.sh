#!/bin/bash

echo "Исправление Jenkins Docker проблемы..."
echo

echo "Шаг 1: Останавливаем все сервисы"
docker compose down
echo

echo "Шаг 2: Удаляем Jenkins volume (если существует)"
if docker volume rm restaurant_jenkins_home 2>/dev/null; then
    echo "Volume restaurant_jenkins_home удален"
else
    echo "Volume restaurant_jenkins_home не найден или уже удален"
fi
echo

echo "Шаг 3: Запускаем Jenkins"
docker compose up -d jenkins
echo

echo "Шаг 4: Ждем запуска Jenkins (30 секунд)..."
sleep 30
echo

echo "Шаг 5: Проверяем статус Jenkins"
docker compose ps jenkins
echo

echo "Шаг 6: Проверяем Docker в Jenkins контейнере"
echo "Подключаемся к Jenkins для проверки Docker..."
if docker compose exec jenkins docker --version >/dev/null 2>&1; then
    echo "УСПЕХ: Docker доступен в Jenkins"
else
    echo "ОШИБКА: Docker не доступен в Jenkins"
    echo "Попробуйте перезапустить Docker Desktop"
fi
echo

echo "Готово! Теперь откройте http://localhost:8088"
echo "И создайте Pipeline с вашим Git репозиторием"
