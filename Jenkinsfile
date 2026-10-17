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
                    snykInstallation: 'Snyk-Latest',
                    snykTokenId: 'SNYK-TOKEN',
                    severity: 'high',
                    failOnIssues: true,
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
