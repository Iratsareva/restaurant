# Используем легковесный образ JRE 21 на базе Alpine Linux
FROM eclipse-temurin:21-jre-alpine

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

# Документируем порт (этот порт должен совпадать с server.port в properties)
EXPOSE 8082

# Запускаем приложение
ENTRYPOINT ["java", "-jar", "app.jar"]

