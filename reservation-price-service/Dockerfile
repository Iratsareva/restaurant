# Stage 1: Build app
FROM maven:3.9.9-eclipse-temurin-17 AS builder

WORKDIR /app

COPY reservation-price-service/pom.xml pom.xml
COPY reservation-price-service/src src
RUN mvn clean package -DskipTests

# Stage 2: Runtime image
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

RUN addgroup -S javauser && adduser -S javauser -G javauser

COPY --from=builder /app/target/*.jar app.jar

RUN chown javauser:javauser /app/app.jar

USER javauser:javauser

EXPOSE 9090
EXPOSE 9091

ENTRYPOINT ["java", "-jar", "/app/app.jar"]