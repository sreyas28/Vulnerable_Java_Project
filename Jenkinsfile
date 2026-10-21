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
                sh 'chmod +x mvnw'
                sh './mvnw clean package -DskipTests'
            }
        }

        stage('Test') {
            steps {
                sh './mvnw test'
            }
        }
    }

    post {
        started {
            slackSend(
                channel: '#jenkins-builds',
                tokenCredentialId: 'slack-token',
                color: '#439FE0',
                message: "STARTED: ${env.JOB_NAME} #${env.BUILD_NUMBER} (<${env.BUILD_URL}|Open>)"
            )
        }
        success {
            slackSend(
                channel: '#jenkins-builds',
                tokenCredentialId: 'slack-token',
                color: 'good',
                message: "SUCCESS: ${env.JOB_NAME} #${env.BUILD_NUMBER} (<${env.BUILD_URL}|Open>)"
            )
        }
        unstable {
            slackSend(
                channel: '#jenkins-builds',
                tokenCredentialId: 'slack-token',
                color: 'warning',
                message: "UNSTABLE: ${env.JOB_NAME} #${env.BUILD_NUMBER} (<${env.BUILD_URL}|Open>)"
            )
        }
        failure {
            slackSend(
                channel: '#jenkins-builds',
                tokenCredentialId: 'slack-token',
                color: 'danger',
                message: "FAILED: ${env.JOB_NAME} #${env.BUILD_NUMBER} (<${env.BUILD_URL}|Open>)"
            )
        }
    }
}
