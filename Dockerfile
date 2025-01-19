# Используем OpenJDK 21 как базовый образ
FROM openjdk:21-slim

# Устанавливаем имя автора
LABEL maintainer="ruslan"

# Устанавливаем рабочую директорию
WORKDIR /app

# Создаем директорию для логов
RUN mkdir -p /app/logs && chmod -R 777 /app/logs

# Копируем jar-файл приложения в контейнер
COPY build/libs/*.jar app-order-service.jar

# Команда для запуска приложения
ENTRYPOINT ["java", "-jar", "app-order-service.jar"]

# Указываем порт, который слушает приложение
EXPOSE 8080
