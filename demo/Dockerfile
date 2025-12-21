# Этап 1: Сборка зависимостей
# Этап 1: Сборка всех зависимостей и demo
FROM maven:3.9.9-eclipse-temurin-17 AS builder

WORKDIR /app

# 1. Собираем и устанавливаем restaurant_api_contracts в локальный Maven-репозиторий
COPY restaurant_api_contracts/pom.xml restaurant_api_contracts/pom.xml
COPY restaurant_api_contracts/src restaurant_api_contracts/src
RUN mvn -f restaurant_api_contracts/pom.xml clean install -DskipTests

# 2. Собираем и устанавливаем events-contract в локальный Maven-репозиторий
COPY events-contract/pom.xml events-contract/pom.xml
COPY events-contract/src events-contract/src
RUN mvn -f events-contract/pom.xml clean install -DskipTests

# 3. Собираем demo, используя артефакты из локального Maven-репозитория
COPY demo/pom.xml demo/pom.xml
COPY demo/src demo/src
COPY demo/src/main/proto demo/src/main/proto
RUN mvn -f demo/pom.xml clean package -DskipTests

# Этап 2: Runtime-образ
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

RUN addgroup -S javauser && adduser -S javauser -G javauser

COPY --from=builder /app/demo/target/*.jar app.jar

RUN chown javauser:javauser /app/app.jar

USER javauser:javauser

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]