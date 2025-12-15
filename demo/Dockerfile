# Этап 1: Сборка зависимостей
FROM maven:3.9.9-eclipse-temurin-17 AS builder-deps

WORKDIR /build

# Сборка restaurant_api_contracts
COPY restaurant_api_contracts/pom.xml restaurant_api_contracts/
COPY restaurant_api_contracts/src restaurant_api_contracts/src/
RUN cd restaurant_api_contracts && mvn clean package -DskipTests

# Сборка events-contract
COPY events-contract/pom.xml events-contract/
COPY events-contract/src events-contract/src/
RUN cd events-contract && mvn clean package -DskipTests

# Этап 2: Сборка demo
FROM maven:3.9.9-eclipse-temurin-17 AS builder-app

WORKDIR /app

# Копируем и собираем demo (с JAR зависимостей)
COPY demo/pom.xml .
COPY demo/src src/
COPY --from=builder-deps /build/restaurant_api_contracts/target/Restaurant-0.0.1-SNAPSHOT.jar lib/restaurant_api_contracts.jar
COPY --from=builder-deps /build/events-contract/target/events-contract-1.0-SNAPSHOT.jar lib/events-contract.jar
RUN mvn clean package -DskipTests

# Этап 3: Runtime-образ
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

RUN addgroup -S javauser && adduser -S javauser -G javauser

USER javauser:javauser

COPY --from=builder-app /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]