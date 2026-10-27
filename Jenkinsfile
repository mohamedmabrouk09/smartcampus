pipeline {
    agent any

    tools {
        maven 'Maven-3.9'
        jdk   'JDK17'
    }

    environment {
        APP_NAME   = 'smartcampus'
        IMAGE_NAME = "${APP_NAME}"
        IMAGE_TAG  = "${env.BUILD_NUMBER}"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build and Test') {
            steps {
                sh 'mvn -B clean verify'
            }
            post {
                always {
                    junit testResults: 'target/surefire-reports/*.xml', allowEmptyResults: true
                }
            }
        }

        stage('Docker Build') {
            steps {
                sh "docker build -t ${IMAGE_NAME}:${IMAGE_TAG} -t ${IMAGE_NAME}:latest ."
            }
        }

        stage('Deploy with Docker Compose') {
            steps {
                sh '''
                    echo "Stopping existing SmartCampus containers if any..."
                    docker compose down || true

                    echo "Starting SmartCampus API + PostgreSQL..."
                    docker compose up -d --build

                    echo "Waiting for services to be ready..."
                    sleep 15

                    echo "Checking /health endpoint..."
                    curl -f http://localhost:8080/health || (echo "Healthcheck failed, showing logs..." && docker compose logs api && exit 1)
                '''
            }
        }
    }

    post {
        success {
            archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
            echo "=================================================="
            echo "SmartCampus deployed successfully!"
            echo "API URL     : http://localhost:8080"
            echo "Health      : http://localhost:8080/health"
            echo "Root        : http://localhost:8080/"
            echo "Login test  : http://localhost:8080/login?email=etudiant@usms.ac.ma&password=Passw0rd!2026"
            echo "Image       : ${IMAGE_NAME}:${IMAGE_TAG} & :latest"
            echo "=================================================="
        }
        failure {
            echo "Pipeline failed. Dumping recent logs..."
            sh 'docker compose logs --tail=80 || true'
        }
        always {
            cleanWs()
        }
    }
}
