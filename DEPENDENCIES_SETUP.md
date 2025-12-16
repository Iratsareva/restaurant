# Настройка зависимостей для переноса проекта

## Проблема
При переносе проекта на другое устройство Maven не может найти локальные зависимости:
- `org.example.restaurant:events-contract:jar:1.0-SNAPSHOT`
- `com.example:Restaurant:jar:0.0.1-SNAPSHOT`

## Решение
Локальные JAR файлы нужно установить в локальный Maven репозиторий.

## Автоматическая установка (Windows)

1. Запустите скрипт `install-dependencies.cmd`:
   ```cmd
   install-dependencies.cmd
   ```

## Ручная установка (для других ОС)

### 1. Установка events-contract
```bash
cd demo
./mvnw install:install-file \
  -Dfile=lib/events-contract.jar \
  -DgroupId=org.example.restaurant \
  -DartifactId=events-contract \
  -Dversion=1.0-SNAPSHOT \
  -Dpackaging=jar
```

### 2. Установка restaurant-api-contracts
```bash
./mvnw install:install-file \
  -Dfile=lib/restaurant_api_contracts.jar \
  -DgroupId=com.example \
  -DartifactId=Restaurant \
  -Dversion=0.0.1-SNAPSHOT \
  -Dpackaging=jar
```

## Проверка установки
После установки проверьте, что файлы появились в локальном репозитории:
- `~/.m2/repository/org/example/restaurant/events-contract/1.0-SNAPSHOT/`
- `~/.m2/repository/com/example/Restaurant/0.0.1-SNAPSHOT/`

## Запуск проекта
После установки зависимостей запустите проект:
```bash
docker compose up -d --build
```

## Важно
- Все JAR файлы находятся в папке `demo/lib/`
- Не удаляйте эти файлы - они нужны для работы проекта
- При изменении контрактов пересоберите их и обновите JAR файлы
