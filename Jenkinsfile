def pom, version, molgenisDocker
pipeline {
    agent any
    tools {
        // Has to be configured on the host with this name : [ mvn-3.5.3 ]
        maven 'mvn-3.5.3'
        // Has to be configured on the host with this name : [ jdk-8u172 ]
        jdk 'jdk-8u172'
    }

    environment {
        DOCKER_HOST = 'tcp://192.168.64.12:2375'
        MOLGENIS_OPERATIONS_DOCKER_ORGANIZATION = 'molgenis-releases'
        MOLGENIS_DOCKER_REGISTRY = 'registry.molgenis.org'
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
                    version = pom.version.replace("-SNAPSHOT", "-${currentBuild.number}-${env.GIT_COMMIT.substring(0, 7)}")
                    currentBuild.displayName = "#${currentBuild.number}.${version}"
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
        stage('Publish package') {
            steps {
                withCredentials([file(credentialsId: 'molgenis.pgp.secretkey', variable: 'KEYFILE'),
                                 string(credentialsId: 'molgenis.pgp.passphrase', variable: 'PASSPHRASE')]) {
                    configFileProvider(
                            [configFile(fileId: 'sonatype-settings', variable: 'MAVEN_SETTINGS')]) {
                        sh "mvn -s ${env.MAVEN_SETTINGS} release:prepare release:perform -B \"-Darguments=-DskipTests -Dpgp.secretkey=keyfile:${env.KEYFILE} -Dpgp.passphrase=literal:${env.PASSPHRASE}\" -DskipTests -DreleaseVersion=${version} -DdevelopmentVersion=${pom.version} -DpushChanges=false -DlocalCheckout=true"
                    }
                }
            }
        }
        stage('Build docker') {
            steps {
                script {
                    stage('Build image') {
                        docker.withTool("docker") {
                            docker.withServer("${DOCKER_HOST}") {
                                echo "Build MOLGENIS docker [ ${MOLGENIS_DOCKER_REGISTRY}/${MOLGENIS_OPERATIONS_DOCKER_ORGANIZATION}/molgenis:lts"
                                molgenisDocker = docker.build("${MOLGENIS_DOCKER_REGISTRY}/${MOLGENIS_OPERATIONS_DOCKER_ORGANIZATION}/molgenis:lts", "--pull --no-cache --force-rm .")
                            }
                        }
                        stage('Push docker') {
                            docker.withTool("docker") {
                                docker.withRegistry("https://${MOLGENIS_DOCKER_REGISTRY}/${MOLGENIS_OPERATIONS_DOCKER_ORGANIZATION}", 'jenkins-registry') {
                                    echo "Publish MOLGENIS docker to [ ${MOLGENIS_DOCKER_REGISTRY} ]"
                                    molgenisDocker.push("latest")
                                    molgenisDocker.push("lts")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    post {
        // [ slackSend ]; has to be configured on the host, it is the "Slack Notification Plugin" that has to be installed
        success {
            notifySuccess()
            build job: 'molgenis-dev-docker', parameters: [[$class: 'StringParameterValue', name: 'version', value: $ {
                version
            }]]
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