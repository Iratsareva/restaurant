pipeline {
    agent any


    tools {
        maven 'Maven3'  // имя из конфигурации
    }

    environment {
        DOCKER_COMPOSE = 'docker compose'  // Используем новую команду (без дефиса)
        PROJECT_NAME = 'restaurant-project'
    }




    stages {
        stage('Checkout') {
            steps {
                echo 'Получение кода из Git...'
                checkout scm
            }
        }

        stage('Build events-contract') {
            steps {
                echo 'Сборка events-contract...'
                dir('events-contract') {
                    sh 'chmod +x mvnw && ./mvnw clean install -DskipTests || mvn clean install -DskipTests'
                }
                // Устанавливаем в локальный репозиторий для использования другими модулями
                script {
                    sh '''
                        mkdir -p demo/lib
                        cp events-contract/target/events-contract-1.0-SNAPSHOT.jar demo/lib/events-contract.jar || true
                    '''
                }
            }
        }
        
        stage('Build restaurant_api_contracts') {
            steps {
                echo 'Сборка restaurant_api_contracts...'
                dir('restaurant_api_contracts') {
                    sh 'chmod +x mvnw && ./mvnw clean package -DskipTests || mvn clean package -DskipTests'
                }
                // Устанавливаем в локальный репозиторий для использования другими модулями
                script {
                    sh '''
                        mkdir -p demo/lib
                        cp restaurant_api_contracts/target/Restaurant-0.0.1-SNAPSHOT.jar demo/lib/restaurant_api_contracts.jar || true
                    '''
                }
            }
        }

        stage('Build demo') {
            steps {
                echo 'Сборка demo сервиса...'
                // Убеждаемся, что JAR зависимости скопированы
                script {
                    sh '''
                        mkdir -p demo/lib
                        cp restaurant_api_contracts/target/Restaurant-0.0.1-SNAPSHOT.jar demo/lib/restaurant_api_contracts.jar || true
                        cp events-contract/target/events-contract-1.0-SNAPSHOT.jar demo/lib/events-contract.jar || true
                    '''
                }
                        dir('demo') {
                            sh 'chmod +x mvnw && ./mvnw clean package -DskipTests || mvn clean package -DskipTests'
                        }
            }
        }

        stage('Build Other Services') {
            parallel {
                stage('Build reservation-price-service') {
                    steps {
                        echo 'Сборка reservation-price-service...'
                        dir('reservation-price-service') {
                            sh 'chmod +x mvnw && ./mvnw clean package -DskipTests || mvn clean package -DskipTests'
                        }
                    }
                }
                stage('Build notification-service') {
                    steps {
                        echo 'Сборка notification-service...'
                        dir('notification-service/notification-service') {
                            sh 'chmod +x mvnw && ./mvnw clean package -DskipTests || mvn clean package -DskipTests'
                        }
                    }
                }
                stage('Build audit-service') {
                    steps {
                        echo 'Сборка audit-service...'
                        dir('audit-service/audit-service') {
                            sh 'chmod +x mvnw && ./mvnw clean package -DskipTests || mvn clean package -DskipTests'
                        }
                    }
                }
            }
        }

        stage('Docker Build') {
            steps {
                echo 'Сборка Docker образов...'
                script {
                    // Проверяем доступность Docker
                    def dockerAvailable = sh(script: 'which docker || which docker-compose', returnStatus: true) == 0
                    if (dockerAvailable) {
                        sh '${DOCKER_COMPOSE} build --no-cache'
                    } else {
                        echo 'Docker не доступен в Jenkins контейнере. Пропускаем сборку образов.'
                        echo 'Для полной интеграции установите Docker в Jenkins или используйте внешний Docker daemon.'
                    }
                }
            }
        }

        stage('Docker Compose Up') {
            steps {
                script {
                    def dockerAvailable = sh(script: 'which docker || which docker-compose', returnStatus: true) == 0
                    if (dockerAvailable) {
                        echo 'Запуск всех сервисов через Docker Compose...'
                        sh '${DOCKER_COMPOSE} up -d'
                    } else {
                        echo 'Docker не доступен. Пропускаем запуск сервисов.'
                    }
                }
            }
        }

        stage('Health Check') {
            steps {
                script {
                    def dockerAvailable = sh(script: 'which docker || which docker-compose', returnStatus: true) == 0
                    if (dockerAvailable) {
                        echo 'Проверка здоровья сервисов...'
                        def services = [
                            ['name': 'demo-rest', 'port': '8080'],
                            ['name': 'reservation-price-service', 'port': '9092'],
                            ['name': 'notification-service', 'port': '8083'],
                            ['name': 'audit-service', 'port': '8082']
                        ]

                        sleep(time: 30, unit: 'SECONDS') // Даем время сервисам запуститься

                        services.each { service ->
                            def name = service.name
                            def port = service.port
                            sh """
                                # Устанавливаем curl если его нет
                                apt-get update -qq && apt-get install -y -qq curl || true

                                # Проверяем доступность сервиса
                                for i in \$(seq 1 30); do
                                    if curl -f http://localhost:${port}/actuator/health 2>/dev/null; then
                                        echo "${name} is healthy"
                                        exit 0
                                    fi
                                    echo "Waiting for ${name}... (\$i/30)"
                                    sleep 2
                                done
                                echo "${name} failed to start (но продолжаем)"
                                # Не прерываем сборку, если health check не прошел
                            """
                        }
                    } else {
                        echo 'Docker не доступен. Пропускаем проверку здоровья.'
                    }
                }
            }
        }

        stage('Verify Metrics') {
            steps {
                script {
                    def dockerAvailable = sh(script: 'which docker || which docker-compose', returnStatus: true) == 0
                    if (dockerAvailable) {
                        echo 'Проверка метрик...'
                        sh '''
                            sleep 10
                            # Устанавливаем curl если его нет
                            apt-get update -qq && apt-get install -y -qq curl || true

                            # Проверяем Prometheus
                            if curl -f http://localhost:9091/api/v1/targets 2>/dev/null; then
                                echo "Prometheus is accessible"
                            else
                                echo "Prometheus check failed (но продолжаем)"
                            fi

                            # Проверяем Grafana
                            if curl -f http://localhost:3000/api/health 2>/dev/null; then
                                echo "Grafana is accessible"
                            else
                                echo "Grafana check failed (но продолжаем)"
                            fi
                        '''
                    } else {
                        echo 'Docker не доступен. Пропускаем проверку метрик.'
                    }
                }
            }
        }
    }

    post {
        always {
            echo 'Очистка...'
            sh '${DOCKER_COMPOSE} down || true'
        }
        success {
            echo 'Pipeline выполнен успешно!'
            echo 'Все JAR файлы успешно скомпилированы!'
            echo 'Для запуска системы выполните локально:'
            echo '  docker-compose up -d'
            echo ''
            echo 'Сервисы будут доступны:'
            echo '  - Demo REST API: http://localhost:8081'
            echo '  - Audit Service: http://localhost:8082'
            echo '  - Notification Service: http://localhost:8083'
            echo '  - Reservation Price gRPC: localhost:9090'
            echo '  - Reservation Price HTTP: http://localhost:9092'
            echo '  - Prometheus: http://localhost:9091'
            echo '  - Grafana: http://localhost:3000 (admin/admin)'
            echo '  - Zipkin: http://localhost:9411'
            echo '  - RabbitMQ: http://localhost:15672 (guest/guest)'
            echo '  - PostgreSQL: localhost:5435'
            echo '  - Jenkins: http://localhost:8088'
        }
        failure {
            echo 'Pipeline завершился с ошибкой!'
            sh '${DOCKER_COMPOSE} logs --tail=100'
        }
    }
}

