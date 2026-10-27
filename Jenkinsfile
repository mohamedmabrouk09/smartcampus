pipeline {
    agent any

    tools {
        maven 'Maven-3.9'
        jdk   'JDK17'
    }

    environment {
        APP_NAME      = 'smartcampus'
        IMAGE_NAME    = "${APP_NAME}"
        IMAGE_TAG     = "${env.BUILD_NUMBER}"
        REGISTRY_REPO = 'yourdockerhubuser'
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
                sh """
                    docker build \
                      -t ${REGISTRY_REPO}/${IMAGE_NAME}:${IMAGE_TAG} \
                      -t ${REGISTRY_REPO}/${IMAGE_NAME}:latest \
                      .
                """
            }
        }
    }

    post {
        success {
            archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
            echo "SmartCampus pipeline completed successfully! Image: ${REGISTRY_REPO}/${IMAGE_NAME}:${IMAGE_TAG}"
        }
        failure {
            echo 'Build, tests, or Docker build failed. Check the logs.'
        }
        always {
            cleanWs()
        }
    }
}
