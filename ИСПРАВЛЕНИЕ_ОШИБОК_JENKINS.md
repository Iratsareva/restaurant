# 🔧 Исправление ошибок в Jenkins Pipeline

## 🚨 Частые ошибки при создании Pipeline "restaurant"

### Ошибка 1: "mvn: command not found" или "Maven не найден"

**Причина:** Maven не установлен или не настроен в Jenkins.

**Решение:**

1. **В Jenkins:**
   - Перейдите: **Manage Jenkins** → **Tools** (или **Global Tool Configuration**)
   - Найдите раздел **"Maven"**
   - Нажмите **"Add Maven"**
   - Заполните:
     - **Name**: `Maven 3.9`
     - ✅ Поставьте галочку **"Install automatically"**
     - **Version**: выберите `3.9.6` (или последнюю стабильную)
   - Нажмите **"Save"**

2. **Или используйте Maven Wrapper:**
   
   В `Jenkinsfile` замените все `mvn` на `./mvnw`:
   
   ```groovy
   // Было:
   sh 'mvn clean install -DskipTests'
   
   // Стало:
   sh './mvnw clean install -DskipTests'
   ```

---

### Ошибка 2: "docker-compose: command not found"

**Причина:** Docker Compose не установлен в контейнере Jenkins.

**Решение:**

1. **Используйте `docker compose` (без дефиса):**
   
   В `Jenkinsfile` замените:
   ```groovy
   // Было:
   DOCKER_COMPOSE = 'docker-compose'
   
   // Стало:
   DOCKER_COMPOSE = 'docker compose'
   ```

2. **Или установите docker-compose в контейнер:**
   
   Обновите `docker-compose.yml`:
   ```yaml
   jenkins:
     image: jenkins/jenkins:lts
     # ... остальные настройки
     command: >
       bash -c "
       apt-get update &&
       apt-get install -y docker-compose-plugin &&
       /usr/local/bin/jenkins.sh
       "
   ```

---

### Ошибка 3: "Cannot connect to Docker daemon"

**Причина:** Jenkins не имеет доступа к Docker socket.

**Решение для Windows:**

1. **Проверьте docker-compose.yml:**
   
   Для Windows Docker Desktop путь к socket может отличаться. Попробуйте:
   
   ```yaml
   jenkins:
     volumes:
       - jenkins_home:/var/jenkins_home
       # Для Windows Docker Desktop:
       - //var/run/docker.sock:/var/run/docker.sock
       # Или используйте Docker-in-Docker:
       - ./:/workspace
     user: root
   ```

2. **Или используйте Docker-in-Docker:**
   
   Обновите `docker-compose.yml`:
   ```yaml
   jenkins:
     image: jenkins/jenkins:lts
     volumes:
       - jenkins_home:/var/jenkins_home
       - ./:/workspace
     privileged: true  # Для Docker-in-Docker
     user: root
   ```

---

### Ошибка 4: "Project not found" или "Directory not found"

**Причина:** Неправильный путь к проекту.

**Решение:**

1. **Если используете Pipeline из Git:**
   
   Убедитесь, что в настройках Pipeline:
   - **Repository URL** указан правильно
   - **Script Path**: `Jenkinsfile` (без пути)
   - **Branch Specifier**: `*/main` или `*/master`

2. **Если используете локальный проект:**
   
   Используйте `Jenkinsfile.local` и убедитесь, что в `docker-compose.yml` есть:
   ```yaml
   volumes:
     - ./:/workspace
   ```

---

### Ошибка 5: "Permission denied" при работе с Docker

**Причина:** Jenkins не имеет прав на выполнение Docker команд.

**Решение:**

1. **Убедитесь, что Jenkins запускается от root:**
   
   В `docker-compose.yml`:
   ```yaml
   jenkins:
     user: root  # Должно быть
   ```

2. **Перезапустите Jenkins:**
   ```bash
   docker-compose restart jenkins
   ```

---

### Ошибка 6: "Port already in use"

**Причина:** Порт 8080 уже занят другим сервисом.

**Решение:**

1. **Остановите конфликтующий сервис:**
   ```bash
   # Найдите, что использует порт 8080
   netstat -ano | findstr :8080  # Windows
   
   # Остановите Jenkins или другой сервис
   docker-compose stop jenkins
   ```

2. **Или измените порт Jenkins:**
   
   В `docker-compose.yml`:
   ```yaml
   jenkins:
     ports:
       - "8081:8080"  # Jenkins будет на порту 8081
   ```
   
   Тогда откройте: http://localhost:8081

---

### Ошибка 7: "Build failed" при сборке Maven проекта

**Причина:** Зависимости не найдены или ошибка компиляции.

**Решение:**

1. **Проверьте логи:**
   - В Jenkins откройте **Console Output**
   - Найдите строку с ошибкой (обычно красным)
   - Скопируйте ошибку

2. **Типичные проблемы:**
   - **"events-contract not found"**: Сначала соберите `events-contract`
   - **"Java version mismatch"**: Убедитесь, что используется правильная версия Java
   - **"Out of memory"**: Увеличьте память для Maven:
     ```groovy
     sh 'export MAVEN_OPTS="-Xmx1024m" && mvn clean install -DskipTests'
     ```

---

### Ошибка 8: Pipeline создан неправильно

**Как правильно создать Pipeline:**

1. **В Jenkins:**
   - Нажмите **"New Item"**
   - Имя: `restaurant` (или любое другое)
   - Выберите **"Pipeline"** (НЕ "Freestyle project"!)
   - Нажмите **"OK"**

2. **Настройка Pipeline:**

   **Вариант A: Из Git (если проект в Git)**
   - **Definition**: **"Pipeline script from SCM"**
   - **SCM**: **"Git"**
   - **Repository URL**: URL вашего репозитория
   - **Branch Specifier**: `*/main`
   - **Script Path**: `Jenkinsfile`

   **Вариант B: Локальный (если проект не в Git)**
   - **Definition**: **"Pipeline script"**
   - Вставьте содержимое файла `Jenkinsfile.local`

3. Нажмите **"Save"**

---

## 🔍 Как найти ошибку в Pipeline

1. **Откройте Jenkins:** http://localhost:8080
2. **Нажмите на ваш Pipeline** (например, `restaurant`)
3. **Нажмите на номер сборки** (например, **#1**)
4. **Нажмите "Console Output"**
5. **Прокрутите вниз** - ошибка будет выделена красным цветом
6. **Скопируйте ошибку** и используйте решения выше

---

## ✅ Проверка правильности настройки

Выполните эти команды для проверки:

```bash
# 1. Проверьте, что Jenkins запущен
docker-compose ps jenkins

# 2. Проверьте логи Jenkins
docker-compose logs jenkins --tail=50

# 3. Проверьте доступ к Docker из Jenkins
docker-compose exec jenkins docker --version

# 4. Проверьте доступ к проекту
docker-compose exec jenkins ls -la /workspace
```

Все команды должны выполниться без ошибок.

---

## 🎯 Быстрое исправление: Обновленный Jenkinsfile для Windows

Если ничего не помогает, используйте этот упрощенный вариант:

```groovy
pipeline {
    agent any

    environment {
        PROJECT_DIR = '/workspace'
        DOCKER_COMPOSE = 'docker compose'  // Без дефиса для новых версий
    }

    stages {
        stage('Checkout') {
            steps {
                echo 'Используем локальный проект...'
                dir(PROJECT_DIR) {
                    sh 'ls -la'
                }
            }
        }

        stage('Build events-contract') {
            steps {
                dir("${PROJECT_DIR}/events-contract") {
                    sh './mvnw clean install -DskipTests || mvn clean install -DskipTests'
                }
            }
        }

        stage('Build Services') {
            parallel {
                stage('Build demo') {
                    steps {
                        dir("${PROJECT_DIR}/demo") {
                            sh './mvnw clean package -DskipTests || mvn clean package -DskipTests'
                        }
                    }
                }
                // ... остальные сервисы аналогично
            }
        }

        stage('Docker Build') {
            steps {
                dir(PROJECT_DIR) {
                    sh '${DOCKER_COMPOSE} build --no-cache'
                }
            }
        }

        stage('Docker Compose Up') {
            steps {
                dir(PROJECT_DIR) {
                    sh '${DOCKER_COMPOSE} up -d'
                }
            }
        }
    }
}
```

---

## 📞 Если ничего не помогает

1. **Полностью пересоздайте Jenkins:**
   ```bash
   docker-compose down
   docker volume rm restaurant-project_jenkins_home  # Осторожно! Удалит все данные
   docker-compose up -d jenkins
   ```

2. **Проверьте версию Docker:**
   ```bash
   docker --version
   docker compose version
   ```

3. **Проверьте, что все файлы на месте:**
   - `Jenkinsfile` или `Jenkinsfile.local` в корне проекта
   - `docker-compose.yml` в корне проекта
   - Все сервисы в правильных папках

---

**Удачи!** 🚀



