def pom, version
pipeline {
    agent any
    tools {
        // Has to be configured on the host with this name : [ mvn-3.5.3 ]
        maven 'mvn-3.5.3'
        // Has to be configured on the host with this name : [ jdk-8u172 ]
        jdk 'jdk-8u172'
    }

    stages {
        stage('Preparation') {
            steps {
                // Clean workspace
                cleanWs()
                // Get code from github/molgenis/molgenis
                checkout scm

                script {
                    pom = readMavenPom file: 'pom.xml'
                    version = pom.version.replace("-SNAPSHOT", "-${currentBuild.number}-${env.GIT_COMMIT.substring(0,7)}")
                    currentBuild.displayName = "#${currentBuild.number}-${version}"
                }

                sh "git config --global user.email molgenis+ci@gmail.com"
                sh "git config --global user.name 'MOLGENIS continuous integration user'"
            }
        }
        stage('Build package') {
            steps {
                sh "mvn package -DskipTests -Dmaven.javadoc.skip=true -B -V -T4"
            }
        }
        stage('Test package') {
            steps {
                parallel(
                        unit: {
                            sh "mvn verify --batch-mode -Dskip.js.build=true -DskipITs"
                            sh "curl -s https://codecov.io/bash | bash -s - -c -F unit"
                            withCredentials(
                                [string(credentialsId: 'jenkins-sonar', variable: 'SONAR_TOKEN')]) {
                                sh "mvn sonar:sonar -Dsonar.login=${SONAR_TOKEN} -Dsonar.branch=${env.GIT_BRANCH} --batch-mode --quiet"
                            }
                        })
//                        api: {
//                            sh "sysctl -w vm.max_map_count=262144"
//                            sh "mvn verify -pl molgenis-api-tests --batch-mode -Dit_db_user=postgres -Dit_db_password"
//                            sh "curl -s https://codecov.io/bash | bash -s - -c -F api"
//                        },
//                        integration: {
//                            sh "mvn verify -pl molgenis-platform-integration-tests --batch-mode -Dit_db_user=postgres -Dit_db_password"
//                            sh "curl -s https://codecov.io/bash | bash -s - -c -F integration"
//                        })
            }
        }
        stage('Publish package') {
            steps {
                configFileProvider(
                    [configFile(fileId: 'sonatype-settings', variable: 'MAVEN_SETTINGS')]) {
                        sh "mvn -s ${env.MAVEN_SETTINGS} release:prepare release:perform -B -Darguments=-DskipTests -DskipTests -DreleaseVersion=${version} -DdevelopmentVersion=${pom.version} -DpushChanges=false -DlocalCheckout=true"
                    }
            }
        }
    }
    post {
        // [ slackSend ]; has to be configured on the host, it is the "Slack Notification Plugin" that has to be installed
        success {
           notifySuccess()
           build job: 'molgenis-dev-docker', parameters: [[$class: 'StringParameterValue', name: 'version', value: ${version}]
        }
        failure {
           notifyFailed()
        }
    }
}

def notifySuccess() {
    slackSend(channel: '#releases', color: '#00FF00', message: "Build success")
}

def notifyFailed() {
    slackSend(channel: '#releases', color: '#FF0000', message: "Build failure")
}