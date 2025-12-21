@echo off
echo 🚀 Запуск сервисов по этапам...

REM Этап 1: Инфраструктура
echo Этап 1: Запуск инфраструктуры...
docker-compose up -d rabbitmq postgres
echo Ожидание готовности инфраструктуры (30 секунд)...
timeout /t 30 /nobreak >nul

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
timeout /t 10 /nobreak >nul
echo ✅ Мониторинг запущен

REM Этап 3: Сервисы
echo Этап 3: Запуск сервисов...
docker-compose up -d reservation-price-service
timeout /t 5 /nobreak >nul
docker-compose up -d notification-service
timeout /t 5 /nobreak >nul
docker-compose up -d audit-service
timeout /t 5 /nobreak >nul
docker-compose up -d demo-rest
timeout /t 10 /nobreak >nul
docker-compose up -d jenkins
echo ✅ Все сервисы запущены

REM Итоговая проверка
echo.
echo 📊 Статус всех сервисов:
docker-compose ps

echo.
echo ✅ Запуск завершен!
pause



