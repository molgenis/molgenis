pipeline {
    agent {
        kubernetes {
            label 'molgenis'
        }
    }
    stages {
        stage('Prepare') {
            steps {
                script {
                    env.GIT_COMMIT = sh(script: 'git rev-parse HEAD', returnStdout: true).trim()
                }
            }
        }
        stage('Package and push snapshot') {
            when {
                changeRequest()
            }
            environment {
                //PR-1234-231
                TAG = "PR-$CHANGE_ID-$BUILD_NUMBER"
                //0.0.0-SNAPSHOT-PR-1234-231
                PREVIEW_VERSION = "0.0.0-SNAPSHOT-$TAG"
            }
            steps {
                container('maven') {
                    sh "mvn versions:set -DnewVersion=$PREVIEW_VERSION -DgenerateBackupPoms=false"
                    sh "mvn clean install -Dmaven.test.redirectTestOutputToFile=true -DskipITs -Ddockerfile.tag=$TAG -Ddockerfile.skip=false"
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
                    sh "curl -s https://codecov.io/bash | bash -s - -c -F unit -K"
                }
            }
        }

        stage('Sonar analysis for change request') {
            when {
                changeRequest()
            }
            steps {
                container('maven') {
                    sh "mvn sonar:sonar -Dsonar.analysis.mode=preview -Dsonar.login=${env.SONAR_TOKEN} -Dsonar.github.oauth=${env.GITHUB_TOKEN} -Dsonar.github.pullRequest=${env.CHANGE_ID} -Dsonar.ws.timeout=120"
                }
            }
        }

        stage('Run platform integration tests') {
            agent {
                kubernetes {
                    label 'molgenis-it'
                }
            }
            steps {
                container('maven') {
                    sh 'mvn verify -pl molgenis-platform-integration-tests -Dmaven.test.redirectTestOutputToFile=true -Dit_db_user=molgenis -Dit_db_password=molgenis'
                }
            }
        }

        stage('Run api tests') {
            agent {
                kubernetes {
                    label 'molgenis-it'
                }
            }
            steps {
                container('maven') {
                    sh 'mvn verify -pl molgenis-api-tests -Dmaven.test.redirectTestOutputToFile=true -Dit_db_user=molgenis -Dit_db_password=molgenis'
                }
            }
        }

    }
}
