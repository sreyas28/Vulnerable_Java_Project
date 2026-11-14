pipeline {
    agent any

    environment {
        DOCKERHUB_CREDS = credentials('dockerhub-creds')
        IMAGE_NAME      = 'sreyassharma/vulnapp'
        IMAGE_TAG       = "${env.BUILD_NUMBER}"
        CONTAINER_NAME  = 'vulnapp'
        HOST_PORT       = '8082'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build Docker Image') {
            steps {
                sh 'docker build -t $IMAGE_NAME:$IMAGE_TAG -t $IMAGE_NAME:latest .'
            }
        }

        stage('Push to Docker Hub') {
            steps {
                sh 'echo $DOCKERHUB_CREDS_PSW | docker login -u $DOCKERHUB_CREDS_USR --password-stdin'
                sh 'docker push $IMAGE_NAME:$IMAGE_TAG'
                sh 'docker push $IMAGE_NAME:latest'
            }
        }

        stage('Deploy to Local Server') {
            steps {
                sh '''
                    docker stop $CONTAINER_NAME || true
                    docker rm $CONTAINER_NAME || true
                    docker run -d \
                        --name $CONTAINER_NAME \
                        -p $HOST_PORT:8080 \
                        -e SERVER_ADDRESS=0.0.0.0 \
                        --restart unless-stopped \
                        $IMAGE_NAME:$IMAGE_TAG
                '''
            }
        }

        stage('Verify Deployment') {
            steps {
                sh 'sleep 25'
                sh 'docker ps --filter name=$CONTAINER_NAME'
                sh 'curl -s -o /dev/null -w "HTTP status: %{http_code}\\n" http://localhost:$HOST_PORT/ || true'
            }
        }
    }

    post {
        always {
            sh 'docker logout || true'
        }
        success {
            echo "Deployed $IMAGE_NAME:$IMAGE_TAG successfully."
        }
        failure {
            echo 'Pipeline failed. Check the console output.'
        }
    }
}
