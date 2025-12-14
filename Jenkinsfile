pipeline {
    agent any

    environment {
        DOCKER_COMPOSE = 'docker-compose'
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
                    sh 'mvn clean install -DskipTests'
                }
            }
        }

        stage('Build Services') {
            parallel {
                stage('Build demo') {
                    steps {
                        echo 'Сборка demo сервиса...'
                        dir('demo') {
                            sh 'mvn clean package -DskipTests'
                        }
                    }
                }
                stage('Build reservation-price-service') {
                    steps {
                        echo 'Сборка reservation-price-service...'
                        dir('reservation-price-service') {
                            sh 'mvn clean package -DskipTests'
                        }
                    }
                }
                stage('Build notification-service') {
                    steps {
                        echo 'Сборка notification-service...'
                        dir('notification-service/notification-service') {
                            sh 'mvn clean package -DskipTests'
                        }
                    }
                }
                stage('Build audit-service') {
                    steps {
                        echo 'Сборка audit-service...'
                        dir('audit-service/audit-service') {
                            sh 'mvn clean package -DskipTests'
                        }
                    }
                }
            }
        }

        stage('Docker Build') {
            steps {
                echo 'Сборка Docker образов...'
                sh '${DOCKER_COMPOSE} build --no-cache'
            }
        }

        stage('Docker Compose Up') {
            steps {
                echo 'Запуск всех сервисов через Docker Compose...'
                sh '${DOCKER_COMPOSE} up -d'
            }
        }

        stage('Health Check') {
            steps {
                echo 'Проверка здоровья сервисов...'
                script {
                    def services = [
                        ['name': 'demo-rest', 'port': '8080'],
                        ['name': 'reservation-price-service', 'port': '9090'],
                        ['name': 'notification-service', 'port': '8083'],
                        ['name': 'audit-service', 'port': '8082']
                    ]
                    
                    sleep(time: 30, unit: 'SECONDS') // Даем время сервисам запуститься
                    
                    services.each { service ->
                        def name = service.name
                        def port = service.port
                        sh """
                            # Проверяем доступность сервиса
                            for i in \$(seq 1 30); do
                                if curl -f http://localhost:${port}/actuator/health 2>/dev/null; then
                                    echo "${name} is healthy"
                                    exit 0
                                fi
                                echo "Waiting for ${name}... (\$i/30)"
                                sleep 2
                            done
                            echo "${name} failed to start"
                            exit 1
                        """
                    }
                }
            }
        }

        stage('Verify Metrics') {
            steps {
                echo 'Проверка метрик...'
                script {
                    sh '''
                        sleep 10
                        # Проверяем Prometheus
                        curl -f http://localhost:9091/api/v1/targets || exit 1
                        echo "Prometheus is accessible"
                        
                        # Проверяем Grafana
                        curl -f http://localhost:3000/api/health || exit 1
                        echo "Grafana is accessible"
                    '''
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
            echo 'Сервисы доступны:'
            echo '  - Demo REST: http://localhost:8080'
            echo '  - Prometheus: http://localhost:9091'
            echo '  - Grafana: http://localhost:3000 (admin/admin)'
            echo '  - Zipkin: http://localhost:9411'
        }
        failure {
            echo 'Pipeline завершился с ошибкой!'
            sh '${DOCKER_COMPOSE} logs --tail=100'
        }
    }
}

