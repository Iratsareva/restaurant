# Настройка Jenkins для проекта

## Предварительные требования

1. Установленный Jenkins
2. Docker и Docker Compose установлены на сервере Jenkins
3. Maven установлен или используется Maven Wrapper

## Шаг 1: Установка необходимых Jenkins плагинов

В Jenkins перейдите в **Manage Jenkins → Manage Plugins** и установите:

- **Pipeline** (обычно уже установлен)
- **Docker Pipeline** (для работы с Docker)
- **Git** (для работы с Git репозиторием)
- **Maven Integration** (для сборки Maven проектов)

## Шаг 2: Настройка Jenkins для работы с Docker

1. Перейдите в **Manage Jenkins → Configure System**
2. Убедитесь, что Docker установлен и доступен
3. В **Global Tool Configuration** настройте:
   - **Maven**: укажите путь к Maven или используйте автоматическую установку
   - **Docker**: укажите путь к Docker (обычно `/usr/bin/docker`)

## Шаг 3: Создание Pipeline Job

1. В Jenkins нажмите **New Item**
2. Выберите **Pipeline**
3. Укажите имя проекта (например, `restaurant-microservices`)
4. В разделе **Pipeline**:
   - **Definition**: выберите **Pipeline script from SCM**
   - **SCM**: выберите **Git**
   - **Repository URL**: укажите URL вашего Git репозитория
   - **Credentials**: добавьте учетные данные для доступа к репозиторию (если требуется)
   - **Branch Specifier**: `*/main` или `*/master` (в зависимости от вашей ветки)
   - **Script Path**: `Jenkinsfile`

## Шаг 4: Настройка прав доступа

Убедитесь, что пользователь Jenkins имеет права на:
- Запуск Docker команд (может потребоваться добавить пользователя в группу `docker`)
- Доступ к портам 8080, 8082, 8083, 9090, 9091, 3000, 9411

Для Linux:
```bash
sudo usermod -aG docker jenkins
sudo systemctl restart jenkins
```

## Шаг 5: Запуск Pipeline

1. Нажмите **Build Now** в созданном Pipeline
2. Следите за выполнением в **Console Output**

## Что делает Pipeline

1. **Checkout** - получает код из Git
2. **Build events-contract** - собирает контракты событий
3. **Build Services** - параллельно собирает все сервисы
4. **Docker Build** - собирает Docker образы
5. **Docker Compose Up** - запускает все сервисы
6. **Health Check** - проверяет здоровье сервисов
7. **Verify Metrics** - проверяет доступность метрик

## Проверка результатов

После успешного выполнения Pipeline:

- **Demo REST**: http://localhost:8080
- **Prometheus**: http://localhost:9091
- **Grafana**: http://localhost:3000 (admin/admin)
- **Zipkin**: http://localhost:9411
- **RabbitMQ Management**: http://localhost:15672 (guest/guest)

## Автоматический запуск при Push в Git

Для автоматического запуска Pipeline при push в репозиторий:

1. В настройках Pipeline включите **Build Triggers → GitHub hook trigger for GITScm polling**
2. В настройках GitHub репозитория добавьте Webhook:
   - **Payload URL**: `http://your-jenkins-server:8080/github-webhook/`
   - **Content type**: `application/json`
   - **Events**: выберите `Just the push event`

## Troubleshooting

### Ошибка: "docker: command not found"
Решение: Убедитесь, что Docker установлен и доступен в PATH для пользователя Jenkins.

### Ошибка: "Permission denied" при работе с Docker
Решение: Добавьте пользователя Jenkins в группу docker (см. Шаг 4).

### Ошибка: "Port already in use"
Решение: Остановите контейнеры: `docker-compose down` или измените порты в docker-compose.yml.

### Ошибка сборки Maven
Решение: Убедитесь, что events-contract установлен в локальный Maven репозиторий перед сборкой сервисов.

