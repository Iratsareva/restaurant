# Stage 1: Build app
FROM maven:3.9.9-eclipse-temurin-17 AS builder

WORKDIR /app

COPY audit-service/audit-service/pom.xml .
COPY audit-service/audit-service/src src/
RUN mvn clean package -DskipTests

# Stage 2: Runtime image
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

RUN addgroup -S javauser && adduser -S javauser -G javauser

USER javauser:javauser

COPY --from=builder /app/target/*.jar app.jar

RUN chown javauser:javauser /app/app.jar

USER javauser:javauser

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "/app/app.jar"]

