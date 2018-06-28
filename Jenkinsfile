pipeline {
    agent {
        kubernetes {
            label 'molgenis-maven'
        }
    }
    environment {
        ORG = 'molgenis'
        APP_NAME = 'molgenis-app'
    }
    stages {
        stage('CI Build and push snapshot') {
            when {
                changeRequest()
            }
            environment {
                //PR-1234-231
                TAG = "$CHANGE_ID-$BUILD_NUMBER"
                //0.0.0-SNAPSHOT-PR-1234-231
                PREVIEW_VERSION = "0.0.0-SNAPSHOT-$TAG"
            }
            steps {
                container('molgenis-maven') {
                    sh "git rev-parse HEAD"
                    sh "mvn -V -B versions:set -DnewVersion=$PREVIEW_VERSION -DgenerateBackupPoms=false"
                    sh "mvn -V -B -T2 clean package -Dmaven.test.redirectTestOutputToFile=true -Ddockerfile.tag=$TAG"
                }
            }
            post {
                always {
                    junit '**/target/surefire-reports/**.xml'
                }
            }
        }

        stage('Unit test coverage') {
            when {
                changeRequest()
            }
            steps {
                container('alpine') {
                    sh "curl -s https://codecov.io/bash | bash -s - -c -F unit -v -K"
                }
            }
        }

        stage('Sonar analysis for change request') {
            when {
                changeRequest()
            }
            steps {
                container('molgenis-maven') {
                    sh "mvn sonar:sonar -B -Dsonar.analysis.mode=preview -Dsonar.login=${env.SONAR_TOKEN} -Dsonar.github.oauth=${env.GITHUB_TOKEN} -Dsonar.github.pullRequest=${env.CHANGE_ID} -Dsonar.ws.timeout=120"
                }
            }
        }
    }
}