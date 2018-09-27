pipeline {
    agent {
        kubernetes {
            label 'molgenis'
        }
    }
    environment {
        npm_config_registry = 'http://nexus.molgenis-nexus:8081/repository/npm-central/'
        DOCKER_LOCAL = 'registry.molgenis.org/molgenis/molgenis-app'
    }
    stages {
        stage('Prepare') {
            steps {
                script {
                    env.GIT_COMMIT = sh(script: 'git rev-parse HEAD', returnStdout: true).trim()
                }
                container('vault') {
                    script {
                        sh "mkdir /home/jenkins/.m2"
                        sh(script: 'vault read -field=value secret/ops/jenkins/maven/settings.xml > /home/jenkins/.m2/settings.xml')
                        env.SONAR_TOKEN = sh(script: 'vault read -field=value secret/ops/token/sonar', returnStdout: true)
                        env.GITHUB_TOKEN = sh(script: 'vault read -field=value secret/ops/token/github', returnStdout: true)
                        env.PGP_PASSPHRASE = 'literal:' + sh(script: 'vault read -field=passphrase secret/ops/certificate/pgp/molgenis-ci', returnStdout: true)
                        sh(script: 'vault read -field=secret.asc secret/ops/certificate/pgp/molgenis-ci > /home/jenkins/key.asc')
                        env.CODECOV_TOKEN = sh(script: 'vault read -field=value secret/ops/token/codecov', returnStdout: true)
                    }
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
                    sh "mvn clean install -Dmaven.test.redirectTestOutputToFile=true -DskipITs -Ddockerfile.tag=${TAG} -Ddockerfile.repository=${DOCKER_LOCAL} -Ddockerfile.skip=false"
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
                TAG = 'dev'
                HELM_REPO = 'https://registry.molgenis.org/repository/helm/'
            }
            steps {
                container('maven') {
                    sh "mvn clean install -Dmaven.test.redirectTestOutputToFile=true -DskipITs -Ddockerfile.tag=${TAG} -Ddockerfile.skip=false"
                    sh "mvn dockerfile:tag dockerfile:push -pl molgenis-app -Ddockerfile.tag=${TAG}-${BUILD_NUMBER} -Ddockerfile.repository=${DOCKER_LOCAL} -Ddockerfile.skip=false"
                    sh "mvn sonar:sonar -Dsonar.login=${SONAR_TOKEN} -Dsonar.branch=${BRANCH_NAME} --batch-mode --quiet -Dsonar.ws.timeout=120"
                }
                milestone(ordinal: 100, label: 'deploy to dev.molgenis.org')
                container('helm') {
                    sh "helm init --client-only"
                    sh "helm repo add molgenis ${HELM_REPO}"
                    sh "helm repo update"
                    sh "helm upgrade molgenis-dev molgenis/molgenis-preview --reuse-values --set molgenis.image.tag=${TAG}-${BUILD_NUMBER} --set molgenis.image.repository=${DOCKER_LOCAL}"
                }
            }
            post {
                always {
                    junit '**/target/surefire-reports/**.xml'
                    container('maven') {
                        sh "curl -s https://codecov.io/bash | bash -s - -c -F unit -K"
                    }
                }
            }
        }
        stage('Build [ x.x ]') {
            when {
                expression { BRANCH_NAME ==~ /[0-9]\.[0-9]/ }
            }
            environment {
                TAG = 'latest'
            }
            steps {
                container('maven') {
                    sh "mvn clean install -Dmaven.test.redirectTestOutputToFile=true -DskipITs -Ddockerfile.tag=${TAG} -Ddockerfile.skip=false"
                    sh "mvn dockerfile:tag dockerfile:push -pl molgenis-app -Ddockerfile.tag=${BRANCH_NAME}-${TAG} -Ddockerfile.skip=false"
                    sh "mvn sonar:sonar -Dsonar.login=${SONAR_TOKEN} -Dsonar.branch=${BRANCH_NAME} --batch-mode --quiet -Dsonar.ws.timeout=120"
                }
            }
            post {
                always {
                    junit '**/target/surefire-reports/**.xml'
                    container('maven') {
                        sh "curl -s https://codecov.io/bash | bash -s - -c -F unit -K"
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
                REPO = 'molgenis'
                MAVEN_ARTIFACT_ID = 'molgenis'
                MAVEN_GROUP_ID = 'org.molgenis'
                PGP_SECRETKEY = "keyfile:/home/jenkins/key.asc"
            }
            steps {
                timeout(time: 40, unit: 'MINUTES') {
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
                milestone(ordinal: 200, label: 'Perform Release')
                container('maven') {
                    sh "git config --global user.email molgenis+ci@gmail.com"
                    sh "git config --global user.name molgenis-jenkins"
                    sh "git remote set-url origin https://${GITHUB_TOKEN}@github.com/${ORG}/${REPO}.git"
                    sh "git checkout -f ${BRANCH_NAME}"
                    sh ".release/generate_release_properties.bash ${MAVEN_ARTIFACT_ID} ${MAVEN_GROUP_ID} ${RELEASE_SCOPE}"
                    sh "mvn release:prepare release:perform -Dmaven.test.redirectTestOutputToFile=true -Darguments=\"-DskipITs -Ddockerfile.tag=${BRANCH_NAME}-${TAG} -Ddockerfile.skip=false\" -DskipITs -Ddockerfile.tag=${BRANCH_NAME}-${TAG} -Ddockerfile.skip=false"
                    sh "git push --tags origin ${BRANCH_NAME}"
                }
            }
        }
    }
}
