# Используем легковесный образ JRE 17 на базе Alpine Linux
FROM eclipse-temurin:17-jre-alpine

# Создаем рабочую директорию
WORKDIR /app

# Создаем пользователя 'javauser', чтобы не запускать приложение от root (Security Best Practice)
RUN addgroup -S javauser && adduser -S javauser -G javauser

# Аргумент для поиска JAR файла.
ARG JAR_FILE=target/*.jar

# Копируем JAR внутрь образа под именем app.jar
COPY ${JAR_FILE} app.jar

# Меняем владельца файла
RUN chown javauser:javauser /app/app.jar

# Переключаемся на пользователя
USER javauser

# Документируем порт (gRPC порт)
EXPOSE 9090

# Запускаем приложение
ENTRYPOINT ["java", "-jar", "app.jar"]

