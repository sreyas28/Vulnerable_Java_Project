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

        stage('Upload to Nexus') {
            steps {
                withCredentials([usernamePassword(
                        credentialsId: 'nexus-creds',
                        usernameVariable: 'NEXUS_USER',
                        passwordVariable: 'NEXUS_PASS')]) {
                    sh './mvnw deploy -s settings.xml -DskipTests'
                }
            }
        }
    }

    post {
        success {
            echo 'Artifact uploaded to Nexus successfully.'
        }
        failure {
            echo 'Pipeline failed. Check the console output.'
        }
    }
}
