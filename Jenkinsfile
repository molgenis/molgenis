def molgenisDocker, version
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
                // TODO: can this be configured higher up?
                sh "git config --global user.email molgenis+ci@gmail.com"
                sh "git config --global user.name 'MOLGENIS continuous integration user'"
            }
        }

        stage('Build, test and deploy to sonatype snapshot repo') {
            environment {
                KEYFILE = credentials('molgenis.pgp.secretkey')
                PASSPHRASE = credentials('molgenis.pgp.passphrase')
            }
            steps {
                configFileProvider([configFile(fileId: 'sonatype-settings', variable: 'MAVEN_SETTINGS')]) {
                    sh "mvn deploy -s ${env.MAVEN_SETTINGS} -V -B -DskipITs -Dpgp.secretkey=keyfile:${env.KEYFILE} -Dpgp.passphrase=literal:${env.PASSPHRASE}"
                }
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

        stage('Sonar analysis for change request') {
            when {
                changeRequest()
            }
            environment {
                SONAR_TOKEN = credentials('jenkins-sonar')
                SONAR_GITHUB_TOKEN = credentials('github.oauth.molgenis-jenkins')
            }
            steps {
                sh "mvn sonar:sonar -B -Dsonar.analysis.mode=preview -Dsonar.login=${env.SONAR_TOKEN} -Dsonar.github.oauth=${env.SONAR_GITHUB_TOKEN} -Dsonar.github.pullRequest=${env.CHANGE_ID} -Dsonar.ws.timeout=120"
            }
        }

        stage('Sonar analysis for branch update') {
            when {
                not {
                    changeRequest()
                }
            }
            environment {
                SONAR_TOKEN = credentials('jenkins-sonar')
            }
            steps {
                sh "mvn sonar:sonar -B -Dsonar.login=${env.SONAR_TOKEN} -Dsonar.branch=${env.GIT_BRANCH}"
            }
        }

        stage('Publish to maven central') {
            when {
                not {
                    changeRequest()
                }
            }
            environment {
                KEYFILE = credentials('molgenis.pgp.secretkey')
                PASSPHRASE = credentials('molgenis.pgp.passphrase')
            }
            steps {
                timeout(time: 5, unit: 'DAYS') {
                    input message: 'Publish to maven central?'
                }
                configFileProvider([configFile(fileId: 'sonatype-settings', variable: 'MAVEN_SETTINGS')]) {
                    sh "mvn -s ${env.MAVEN_SETTINGS} release:prepare release:perform -B \"-Darguments=-DskipTests -Dpgp.secretkey=keyfile:${env.KEYFILE} -Dpgp.passphrase=literal:${env.PASSPHRASE}\" -DskipTests -DdevelopmentVersion=${pom.version} -DpushChanges=false -DlocalCheckout=true"
                }
            }
        }

        // todo: move one step up
        stage('Build docker') {
            environment {
                HOST = 'tcp://192.168.64.12:2375'
                ORGANIZATION = 'molgenis-releases'
                REGISTRY = 'registry.molgenis.org'
            }
            steps {
                script {
                    stage('Build image') {
                        docker.withTool("docker") {
                            docker.withServer("${env.HOST}") {
                                echo "Build MOLGENIS docker [ ${env.REGISTRY}/${env.ORGANIZATION}/molgenis:lts"
                                molgenisDocker = docker.build("${env.REGISTRY}/${env.ORGANIZATION}/molgenis:lts", "--pull --no-cache --force-rm .")
                            }
                        }
                        stage('Push docker') {
                            docker.withTool("docker") {
                                docker.withRegistry("https://${env.REGISTRY}/${env.ORGANIZATION}", 'jenkins-registry') {
                                    echo "Publish MOLGENIS docker to [ ${env.REGISTRY} ]"
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
            build job: 'molgenis-dev-docker', parameters: [[$class: 'StringParameterValue', name: 'version', value: "${version}"]]
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