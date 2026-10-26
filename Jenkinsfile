pipeline {
    agent any
    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        stage('Build') {
            steps {
                sh 'mvn -B clean package -DskipTests'
            }
        }
        stage('Test') {
            steps {
                sh 'mvn -B test'
            }
        }
        stage('Docker Build') {
            steps {
                sh 'docker build -t smartcampus:1.0 .'
            }
        }
    }

    post {
        success {
            archiveArtifacts artifacts: 'target/smartcampus.jar', fingerprint: true
        }
        failure {
            echo 'Build or tests failed — check the logs.'
        }
    }
}