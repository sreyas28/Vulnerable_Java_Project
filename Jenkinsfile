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

        stage('Snyk Security Scan') {
            steps {
                snykSecurity(
                    snykInstallation: 'snyk-latest',
                    snykTokenId: 'snyk-token',
                    severity: 'high',
                    failOnIssues: false,
                    monitorProjectOnBuild: true
                )
            }
        }
    }

    post {
        always {
            echo 'Pipeline finished.'
        }
    }
}
