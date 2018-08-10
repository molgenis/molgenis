pipeline {
    agent {
        kubernetes {
            label 'molgenis'
        }
    }
    environment {
        SAUCELABS_CRED = credentials('molgenis-jenkins-saucelabs-secret')
    }
    stages {
        stage('Prepare') {
            steps {
                script {
                    env.GIT_COMMIT = sh(script: 'git rev-parse HEAD', returnStdout: true).trim()
                }
            }
        }
        stage('Build [ pull request ]') {
            when {
                changeRequest()
            }
            environment {
                //PR-1234-231
                TAG = "PR-${CHANGE_ID}-${BUILD_NUMBER}"
                //0.0.0-SNAPSHOT-PR-1234-231
                PREVIEW_VERSION = "0.0.0-SNAPSHOT-${TAG}"
            }
            steps {
                container('maven') {
                    sh "mvn versions:set -DnewVersion=${PREVIEW_VERSION} -DgenerateBackupPoms=false"
                    sh "mvn clean install -Dmaven.test.redirectTestOutputToFile=true -DskipITs -Ddockerfile.tag=${TAG} -Ddockerfile.skip=false"
                }
            }
            post {
                always {
                    junit '**/target/surefire-reports/**.xml'
                    container('maven') {
                        sh "curl -s https://codecov.io/bash | bash -s - -c -F unit -K"
                        sh "mvn sonar:sonar -Dsonar.analysis.mode=preview -Dsonar.login=${env.SONAR_TOKEN} -Dsonar.github.oauth=${env.GITHUB_TOKEN} -Dsonar.github.pullRequest=${env.CHANGE_ID} -Dsonar.ws.timeout=120"
                    }
                }
            }
        }

        stage('Build [ master ]') {
            when {
                branch 'master'
            }
            environment {
                TAG = 'latest'
            }
            steps {
                container('maven') {
                    sh "mvn clean install -Dmaven.test.redirectTestOutputToFile=true -DskipITs -Ddockerfile.tag=${TAG} -Ddockerfile.skip=false"
                }
            }
            post {
                always {
                    junit '**/target/surefire-reports/**.xml'
                    container('maven') {
                        sh "curl -s https://codecov.io/bash | bash -s - -c -F unit -K"
                        sh "mvn sonar:sonar -Dsonar.analysis.mode=preview -Dsonar.login=${env.SONAR_TOKEN} -Dsonar.github.oauth=${env.GITHUB_TOKEN} -Dsonar.github.pullRequest=${env.CHANGE_ID} -Dsonar.ws.timeout=120"
                    }
                }
            }
        }
        stage('Build [ x.x ]') {
            when {
                expression { BRANCH_NAME ==~ /[0-9]\.[0-9]/ }
            }
            environment {
                TAG = 'stable'
            }
            steps {
                container('maven') {
                    sh "mvn clean install -Dmaven.test.redirectTestOutputToFile=true -DskipITs -Ddockerfile.tag=${BRANCH_NAME}-${TAG} -Ddockerfile.skip=false"
                }
            }
            post {
                always {
                    junit '**/target/surefire-reports/**.xml'
                    container('maven') {
                        sh "curl -s https://codecov.io/bash | bash -s - -c -F unit -K"
                        sh "mvn sonar:sonar -Dsonar.analysis.mode=preview -Dsonar.login=${env.SONAR_TOKEN} -Dsonar.github.oauth=${env.GITHUB_TOKEN} -Dsonar.github.pullRequest=${env.CHANGE_ID} -Dsonar.ws.timeout=120"
                    }
                }
            }
        }
        stage('Release [ x.x ]') {
            when {
                expression { BRANCH_NAME ==~ /[0-9]\.[0-9]/ }
            }
            environment {
                TAG = 'stable'
                ORG = 'molgenis'
                GROUP_NAME = 'org.molgenis'
                APP_NAME = 'molgenis-app-maven-test'
                GITHUB_CRED = credentials('molgenis-jenkins-github-secret')
            }
            steps {
                timeout(time: 10, unit: 'MINUTES') {
                    script {
                        env.RELEASE_SCOPE = input(
                                message: 'Do you want to release?',
                                ok: 'Release',
                                parameters: [
                                        choice(choices: 'candidate\nrelease', description: '', name: 'RELEASE_SCOPE')
                                ]
                        )
                    }
                }
                milestone 1
                container('maven') {
                    sh "git config --global user.email git@molgenis.org"
                    sh "git config --global user.name ${env.GITHUB_CRED_USR}"
                    sh "git remote set-url origin https://${env.GITHUB_CRED_PSW}@github.com/${GROUP_NAME}/${APP_NAME}.git"
                    sh "git checkout -f ${BRANCH_NAME}"
                    sh ".release/generate_release_properties.bash ${APP_NAME} ${GROUP_NAME} ${env.RELEASE_SCOPE}"
                    sh "mvn release:prepare release:perform -Dmaven.test.redirectTestOutputToFile=true -DskipITs -Ddockerfile.tag=${BRANCH_NAME}-${TAG} -Ddockerfile.skip=false"
                    sh "git push --tags origin ${BRANCH_NAME}"
                }
            }
        }
    }
}
