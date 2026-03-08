pipeline {
    agent any

    environment {
        MAVEN_HOME        = tool 'Maven'
        SONAR_HOST_URL    = 'http://your-sonarqube-server:9000'
        SONAR_PROJECT_KEY = 'your-project-key'
        DEPLOY_SERVER     = 'user@your-deploy-server'
        DEPLOY_PATH       = '/opt/apps/your-app'
        APP_NAME          = 'your-app'
    }

    tools {
        maven 'Maven'
        jdk   'JDK17'
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                sh 'mvn clean package -DskipTests'
            }
            post {
                success {
                    echo 'Build successful.'
                    archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
                }
            }
        }

        stage('Test') {
            steps {
                sh 'mvn test'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                    jacoco(
                        execPattern:    'target/jacoco.exec',
                        classPattern:   'target/classes',
                        sourcePattern:  'src/main/java'
                    )
                }
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('SonarQube') {
                    sh """
                        mvn sonar:sonar \
                            -Dsonar.projectKey=${SONAR_PROJECT_KEY} \
                            -Dsonar.host.url=${SONAR_HOST_URL} \
                            -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml
                    """
                }
            }
        }

        stage('Quality Gate') {
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        stage('Deploy') {
            when {
                branch 'main'
            }
            steps {
                withCredentials([sshUserPrivateKey(
                    credentialsId: 'deploy-ssh-key',
                    keyFileVariable: 'SSH_KEY'
                )]) {
                    sh """
                        JAR=\$(ls target/*.jar | head -1)

                        # Copy artifact to server
                        scp -i \$SSH_KEY -o StrictHostKeyChecking=no \
                            \$JAR ${DEPLOY_SERVER}:${DEPLOY_PATH}/

                        # Restart application
                        ssh -i \$SSH_KEY -o StrictHostKeyChecking=no ${DEPLOY_SERVER} '
                            cd ${DEPLOY_PATH}
                            ln -sf \$(ls -t *.jar | head -1) ${APP_NAME}.jar
                            systemctl restart ${APP_NAME} || \
                                (pkill -f "${APP_NAME}.jar"; nohup java -jar ${APP_NAME}.jar &)
                        '
                    """
                }
            }
        }
    }

    post {
        success {
            echo "Pipeline completed successfully."
        }
        failure {
            echo "Pipeline failed. Check the logs."
            // emailext(
            //     subject: "BUILD FAILED: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
            //     body:    "Check: ${env.BUILD_URL}",
            //     to:      'team@example.com'
            // )
        }
        always {
            cleanWs()
        }
    }
}
