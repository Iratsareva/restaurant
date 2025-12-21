#!/bin/bash

# Запускаем Docker daemon в фоне (от root)
dockerd --host=unix:///var/run/docker.sock --host=tcp://0.0.0.0:2376 &

# Ждем пока Docker daemon запустится
echo "Waiting for Docker daemon to start..."
sleep 10

# Проверяем что Docker работает
if docker info >/dev/null 2>&1; then
    echo "Docker daemon is running"
else
    echo "Docker daemon failed to start"
    exit 1
fi

# Создаем пользователя jenkins если его нет
if ! id jenkins >/dev/null 2>&1; then
    adduser -D -h /var/jenkins_home -s /bin/bash jenkins
    addgroup jenkins docker
    chown -R jenkins:jenkins /var/jenkins_home
fi

# Устанавливаем переменные окружения для Jenkins
export JENKINS_HOME=/var/jenkins_home
export JAVA_OPTS="-Djenkins.install.runSetupWizard=false"

# Запускаем Jenkins от имени jenkins пользователя
echo "Starting Jenkins..."
exec su-exec jenkins java -jar /usr/share/jenkins/jenkins.war --httpPort=8080 --prefix=""
