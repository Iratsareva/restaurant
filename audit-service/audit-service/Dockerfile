# Stage 1: Build app (включая зависимость events-contract)
FROM maven:3.9.9-eclipse-temurin-17 AS builder

WORKDIR /app

# Сначала собираем и устанавливаем events-contract в локальный Maven-репозиторий
COPY events-contract/pom.xml events-contract/pom.xml
COPY events-contract/src events-contract/src
RUN mvn -f events-contract/pom.xml clean install -DskipTests

# Затем собираем сам сервис аудита
COPY audit-service/audit-service/pom.xml pom.xml
COPY audit-service/audit-service/src src
RUN mvn clean package -DskipTests

# Stage 2: Runtime image
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

RUN addgroup -S javauser && adduser -S javauser -G javauser

COPY --from=builder /app/target/*.jar app.jar

RUN chown javauser:javauser /app/app.jar

USER javauser:javauser

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "/app/app.jar"]