def molgenisDocker
pipeline {
    agent any
    tools {
        // Have to be configured on the host with these names
        maven 'mvn-3.5.3'
        jdk 'jdk-8u172'
    }
    stages {
        stage('Preparation') {
            steps {
                sh "git config --global user.email molgenis+ci@gmail.com"
                sh "git config --global user.name 'MOLGENIS continuous integration user'"
            }
        }

        stage('Build package') {
            steps {
                sh "mvn package -B -DskipTests -T4"
            }
        }

        stage('Unit test') {
            steps {
                sh "mvn verify -B -Dskip.js.build=true -DskipITs"
            }
            post {
                always {
                    junit '**/target/surefire-reports/junitreports/TEST-*.xml'
                }
            }
        }

        stage('Unit test coverage') {
            environment {
                CODECOV_TOKEN = credentials('jenkins-codecov')
            }
            steps {
                sh "curl -s https://codecov.io/bash | bash -s - -t ${env.CODECOV_TOKEN} -c -F unit"
            }
        }

        stage('Sonar analysis') {
            environment {
                SONAR_TOKEN = credentials('jenkins-sonar')
                SONAR_GITHUB_TOKEN = credentials('jenkins-github')
            }
            steps {
                sh "mvn sonar:sonar -Dsonar.analysis.mode=preview -Dsonar.login=${env.SONAR_TOKEN} -Dsonar.github.oauth=${env.SONAR_GITHUB_TOKEN} -Dsonar.github.pullRequest=${env.GIT_BRANCH} -Dsonar.ws.timeout=120"
                // TODO: Run this instead for merged code
                // sh "mvn sonar:sonar -B -Dsonar.login=${env.SONAR_TOKEN} -Dsonar.branch=${env.GIT_BRANCH}"
            }
        }

        stage('Deploy to sonatype snapshot repository') {
            steps {
                sh "mvn deploy -B"
            }
        }

        stage('Publish to') {
            environment {
                KEYFILE = credentials('molgenis.pgp.secretkey')
                PASSPHRASE = credentials('molgenis.pgp.passphrase')
            }
            steps {
                timeout(time: 5, unit: 'DAYS') {
                    input message: 'Release this build?'
                }
                configFileProvider([configFile(fileId: 'sonatype-settings', variable: 'MAVEN_SETTINGS')]) {
                    sh "mvn -s ${env.MAVEN_SETTINGS} release:prepare release:perform -B \"-Darguments=-DskipTests -Dpgp.secretkey=keyfile:${env.KEYFILE} -Dpgp.passphrase=literal:${env.PASSPHRASE}\" -DskipTests -DreleaseVersion=${version} -DdevelopmentVersion=${pom.version} -DpushChanges=false -DlocalCheckout=true"
                }
            }
        }

        stage('Build docker') {
            environment {
                DOCKER_HOST = 'tcp://192.168.64.12:2375'
                ORGANIZATION = 'molgenis-releases'
                REGISTRY = 'registry.molgenis.org'
            }
            steps {
                script {
                    stage('Build image') {
                        docker.withTool("docker") {
                            docker.withServer("${DOCKER_HOST}") {
                                echo "Build MOLGENIS docker [ ${REGISTRY}/${ORGANIZATION}/molgenis:lts"
                                molgenisDocker = docker.build("${REGISTRY}/${ORGANIZATION}/molgenis:lts", "--pull --no-cache --force-rm .")
                            }
                        }
                        stage('Push docker') {
                            docker.withTool("docker") {
                                docker.withRegistry("https://${REGISTRY}/${ORGANIZATION}", 'jenkins-registry') {
                                    echo "Publish MOLGENIS docker to [ ${REGISTRY} ]"
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
        always {
            cleanWs()
        }
    }
}

def notifySuccess() {
    slackSend(channel: '#releases', color: '#00FF00', message: "Build success")
}

def notifyFailed() {
    slackSend(channel: '#releases', color: '#FF0000', message: "Build failure")
}