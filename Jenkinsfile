pipeline {
    agent any
    tools {
        // Has to be configured on the host with this name : [ mvn-3.5.3 ]
        maven 'mvn-3.5.3'
    }

    environment {
        MOLGENIS_VERSION = "7.0.0-SNAPSHOT"
    }

    stages {
        stage('Preparation') {
            steps {
                // Clean workspace
                cleanWs()
                // Get code from git.webhosting.rug.nl
                checkout scm
            }
        }
        stage('Build package without tests') {
            steps {
                echo "Build MOLGENIS"
                sh "mvn clean package -DskipTests"
            }
        }
        stage('Publish package') {
            steps {
                echo "Publish MOLGENIS to download-server (https://molgenis26.gcc.rug.nl/)"
                sh "scp target/molgenis-app-${MOLGENIS_VERSION}.war molgenis@molgenis26.gcc.rug.nl:/releases/molgenis/${MOLGENIS_VERSION}/"
            }
        }
    }
    post {
        // [ slackSend ]; has to be configured on the host, it is the "Slack Notification Plugin" that has to be installed
        success {
            notifySuccess()
        }
        failure {
            notifyFailed()
        }
    }
}

def notifySuccess() {
    slackSend(channel: '#releases', color: '#00FF00', message: "RPM-build is successfully deployed on ${MOLGENIS_REPOSITORY}: Job - <${env.BUILD_URL}|${env.JOB_NAME}> | #${env.BUILD_NUMBER}")
}

def notifyFailed() {
    slackSend(channel: '#releases', color: '#FF0000', message: "RPM-build has failed: Job - <${env.BUILD_URL}|${env.JOB_NAME}> | #${env.BUILD_NUMBER}")
}