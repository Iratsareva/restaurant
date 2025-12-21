@echo off
echo 🔍 Диагностика Docker Compose...
echo.

REM Проверка Docker
echo 1. Проверка Docker...
docker --version >nul 2>&1
if errorlevel 1 (
    echo ❌ Docker не установлен!
    exit /b 1
)
echo ✅ Docker установлен

REM Проверка Docker Compose
echo 2. Проверка Docker Compose...
docker-compose --version >nul 2>&1
if errorlevel 1 (
    echo ❌ Docker Compose не установлен!
    exit /b 1
)
echo ✅ Docker Compose установлен

REM Проверка статуса контейнеров
echo.
echo 3. Статус контейнеров:
docker-compose ps

REM Проверка портов
echo.
echo 4. Проверка занятых портов:
netstat -ano | findstr ":8080 :8081 :8082 :8083 :9090 :9091 :3000 :9411 :5672 :15672"
if errorlevel 1 (
    echo Все порты свободны
)

REM Проверка логов
echo.
echo 5. Последние ошибки в логах:
docker-compose logs --tail=20 2>&1 | findstr /i "error"
if errorlevel 1 (
    echo Ошибок не найдено
)

REM Проверка сети
echo.
echo 6. Проверка Docker сети:
docker network ls | findstr microservices
if errorlevel 1 (
    echo Сеть не найдена
)

REM Проверка volumes
echo.
echo 7. Проверка volumes:
docker volume ls | findstr restaurant
if errorlevel 1 (
    echo Volumes не найдены
)

echo.
echo ✅ Диагностика завершена!
echo.
echo Для просмотра полных логов выполните:
echo   docker-compose logs

pause



