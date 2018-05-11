@Library('zion-pipeline-library')
import com.sonatype.jenkins.pipeline.GitHub

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

                commitId = OsTools.runSafe(this, 'git rev-parse HEAD')
                commitDate = OsTools.runSafe(this, "git show -s --format=%cd --date=format:%Y%m%d-%H%M%S ${commitId}")

                sh "git config --global user.email molgenis+ci@gmail.com"
                sh "git config --global user.name 'MOLGENIS continuous integration user'"

                pom = readMavenPom file: 'pom.xml'
                version = pom.version.replace("-SNAPSHOT", ".${commitDate}.${commitId.substring(0, 7)}")

                currentBuild.displayName = "#${currentBuild.number} - ${version}"

                def apiToken
                withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'jenkins-github', usernameVariable: 'GITHUB_API_USERNAME', passwordVariable: 'GITHUB_API_PASSWORD']]) {
                    apiToken = env.GITHUB_API_PASSWORD
                }
                gitHub = new GitHub(this, 'molgenis/molgenis', apiToken)
            }
        }
        stage('Build package') {
            steps {
                gitHub.statusUpdate commitId, 'pending', 'build', 'Building'
                sh "mvn package -DskipTests -Dmaven.javadoc.skip=true -B -V -T4"
            }
        }
        stage('Test package') {
            steps {
                gitHub.statusUpdate commitId, 'pending', 'tests', 'Unit, API and integration tests'
                parallel(
                        unit: {

                            sh "mvn verify --batch-mode -Dskip.js.build=true -DskipITs"
                            sh "curl -s https://codecov.io/bash | bash -s - -c -F unit"
                            sh ".travis/sonar.sh"
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
                        gitHub.statusUpdate commitId, 'pending', 'publish', 'Publishing to Sonatype'
                        sh "mvn -s ${env.MAVEN_SETTINGS} release:prepare release:perform -B -DskipTests -DreleaseVersion=${version} -DdevelopmentVersion=${pom.version} -DpushChanges=false -DlocalCheckout=true"
                    }
            }
        }
    }
    post {
        // [ slackSend ]; has to be configured on the host, it is the "Slack Notification Plugin" that has to be installed
        success {
           gitHub.statusUpdate commitId, 'success', 'success', 'Build success'
           notifySuccess()
           build: 'molgenis'
        }
        failure {
           gitHub.statusUpdate commitId, 'failure', 'failure', 'Build failed'
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