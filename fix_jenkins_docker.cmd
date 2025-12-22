@echo off
echo Исправление Jenkins Docker-in-Docker проблемы...
echo.

echo Шаг 1: Останавливаем все сервисы
docker compose down
echo.

echo Шаг 2: Удаляем Jenkins volumes (если существуют)
docker volume rm restaurant_jenkins_home 2>nul
docker volume rm restaurant_jenkins_docker_data 2>nul
echo Jenkins volumes очищены
echo.

echo Шаг 3: Пересобираем Jenkins образ (--no-cache, исправлена система пакетов)
docker compose build --no-cache jenkins
echo.

echo Шаг 4: Запускаем Jenkins
docker compose up -d jenkins
echo.

echo Шаг 5: Ждем запуска Jenkins (60 секунд)...
timeout /t 60 /nobreak > nul
echo.

echo Шаг 6: Проверяем статус Jenkins
docker compose ps jenkins
echo.

echo Шаг 7: Проверяем что Jenkins запустился
docker compose exec jenkins ps aux | findstr java
if %errorlevel% neq 0 (
    echo ОШИБКА: Jenkins не запустился
    echo Проверьте логи: docker compose logs jenkins
) else (
    echo УСПЕХ: Jenkins процесс запущен
)
echo.

echo Шаг 8: Проверяем Docker в Jenkins контейнере
docker compose exec jenkins docker --version
if %errorlevel% neq 0 (
    echo ОШИБКА: Docker не доступен в Jenkins
    echo Проверьте логи: docker compose logs jenkins
) else (
    echo УСПЕХ: Docker доступен в Jenkins
)
echo.

echo Готово! Теперь откройте http://localhost:8088
echo И создайте Pipeline с вашим Git репозиторием
pause
