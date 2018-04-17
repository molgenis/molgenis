pipeline {
    agent any
    tools {
        // Has to be configured on the host with this name : [ mvn-3.5.3 ]
        maven 'mvn-3.5.3'
        // Has to be configured on the host with this name : [ jdk-8u162 ]
        jdk 'jdk-8u162'
    }

    environment {
        JAVA_HOME = "${tool 'jdk-8u162'}"
        PATH = "${JAVA_HOME}/bin:${PATH}"
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
        stage('Build package') {
            steps {
                echo "Build MOLGENIS"
                sh "mvn install -DskipTests -Dmaven.javadoc.skip=true -B -V -T4"
            }
        }
        stage('Test package') {
            steps {
                parallel(
                        unit: {
                            sh "mvn verify --batch-mode --quiet -Dmaven.test.redirectTestOutputToFile=true -Dskip.js.build=true -DskipITs"
                            sh "bash < (curl - s https://codecov.io/bash) -c -F unit"
                            sh ".travis / sonar.sh"
                        })
//                        api: {
//                            sh "sysctl -w vm.max_map_count=262144"
//                            sh "mvn verify -pl molgenis -api - tests-- batch -mode-- quiet -Dmaven.test.redirectTestOutputToFile = true - Dit_db_user = postgres - Dit_db_password "
//                            sh "bash < (curl - s https://codecov.io/bash) -c -F unit"
//                        },
//                        integration: {
//                            sh "mvn verify -pl molgenis-platform-integration-tests --batch-mode --quiet -Dmaven.test.redirectTestOutputToFile=true -Dit_db_user=postgres -Dit_db_password"
//                            sh "bash < (curl - s https://codecov.io/bash) -c -F unit"
//                        })
            }
        }
//        stage('Publish package') {
//            steps {
//                configFileProvider(
//                        [configFile(fileId: 'maven-sonatype-settings', variable: 'MAVEN_SETTINGS')]) {
//                    sh "mvn deploy"
//                }
//            }
//        }
    }
//    post {
// [ slackSend ]; has to be configured on the host, it is the "Slack Notification Plugin" that has to be installed
//        success {
///           notifySuccess()
//            build: 'molgenis-dev-docker'
//        }
//    }
}

def notifySuccess() {
    slackSend(channel: '#releases', color: '#00FF00', message: "RPM-build is successfully deployed on ${MOLGENIS_REPOSITORY}: Job - <${env.BUILD_URL}|${env.JOB_NAME}> | #${env.BUILD_NUMBER}")
}

def notifyFailed() {
    slackSend(channel: '#releases', color: '#FF0000', message: "RPM-build has failed: Job - <${env.BUILD_URL}|${env.JOB_NAME}> | #${env.BUILD_NUMBER}")
}