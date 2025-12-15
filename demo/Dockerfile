# Используем легковесный образ JRE 17 на базе Alpine Linux
FROM eclipse-temurin:17-jre-alpine

# Создаем рабочую директорию
WORKDIR /app

# Создаем пользователя 'javauser', чтобы не запускать приложение от root
RUN addgroup -S javauser && adduser -S javauser -G javauser

# Копируем JAR файл (Spring Boot fat JAR уже содержит все зависимости)
COPY target/*.jar app.jar

# Копируем дополнительные JAR зависимости
COPY lib/*.jar /app/lib/

# Меняем владельца файлов
RUN chown -R javauser:javauser /app

# Переключаемся на пользователя
USER javauser

# Документируем порт
EXPOSE 8080

# Запускаем приложение
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

