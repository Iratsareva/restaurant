@echo off
echo Исправление Jenkins Docker проблемы...
echo.

echo Шаг 1: Останавливаем все сервисы
docker compose down
echo.

echo Шаг 2: Удаляем Jenkins volume (если существует)
docker volume rm restaurant_jenkins_home 2>nul
if %errorlevel% neq 0 (
    echo Volume restaurant_jenkins_home не найден или уже удален
) else (
    echo Volume restaurant_jenkins_home удален
)
echo.

echo Шаг 3: Запускаем Jenkins
docker compose up -d jenkins
echo.

echo Шаг 4: Ждем запуска Jenkins (30 секунд)...
timeout /t 30 /nobreak > nul
echo.

echo Шаг 5: Проверяем статус Jenkins
docker compose ps jenkins
echo.

echo Шаг 6: Проверяем Docker в Jenkins контейнере
echo Подключаемся к Jenkins для проверки Docker...
docker compose exec jenkins docker --version
if %errorlevel% neq 0 (
    echo ОШИБКА: Docker не доступен в Jenkins
    echo Попробуйте перезапустить Docker Desktop
) else (
    echo УСПЕХ: Docker доступен в Jenkins
)
echo.

echo Готово! Теперь откройте http://localhost:8088
echo И создайте Pipeline с вашим Git репозиторием
pause
