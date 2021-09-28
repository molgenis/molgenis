pipeline {
    agent {
        kubernetes {
            // the shared pod template defined on the Jenkins server config
            inheritFrom 'shared'
            // the specific build config defined in this repo
            yamlFile ".jenkins/build-pod.yaml"
        }
    }
    environment {
        LOCAL_REPOSITORY = "${LOCAL_REGISTRY}/molgenis/molgenis-app"
        CHART_VERSION = '1.15.8'
        TIMESTAMP = sh(returnStdout: true, script: "date -u +'%F_%H-%M-%S'").trim()
    }
    stages {
        stage('Retrieve build secrets') {
            steps {
                container('vault') {
                    script {
                        sh "mkdir ${JENKINS_AGENT_WORKDIR}/.m2"
                        sh "mkdir ${JENKINS_AGENT_WORKDIR}/.rancher"
                        sh(script: "vault read -field=value secret/ops/jenkins/rancher/cli2.json > ${JENKINS_AGENT_WORKDIR}/.rancher/cli2.json")
                        sh(script: "vault read -field=value secret/ops/jenkins/maven/settings.xml > ${JENKINS_AGENT_WORKDIR}/.m2/settings.xml")
                        env.SONAR_TOKEN = sh(script: 'vault read -field=value secret/ops/token/sonar', returnStdout: true)
                        env.GITHUB_TOKEN = sh(script: 'vault read -field=value secret/ops/token/github', returnStdout: true)
                        env.GITHUB_USER = sh(script: 'vault read -field=username secret/ops/token/github', returnStdout: true)
                    }
                }
                dir("${JENKINS_AGENT_WORKDIR}/.m2") {
                    stash includes: 'settings.xml', name: 'maven-settings'
                }
                dir("${JENKINS_AGENT_WORKDIR}/.rancher") {
                    stash includes: 'cli2.json', name: 'rancher-config'
                }
            }
        }
        stage('Steps [ PR ]') {
            when {
                changeRequest()
            }
            environment {
                // PR-1234-23
                TAG = "PR-${CHANGE_ID}-${BUILD_NUMBER}"
            }
            stages {
                stage('Build, Test, Push to Registries [ PR ]') {
                    steps {
                        container('maven') {
                            script {
                                // 8.5.0-SNAPSHOT-PR-1234-23
                                env.PREVIEW_VERSION = sh(script: "grep version pom.xml | grep SNAPSHOT | cut -d'>' -f2 | cut -d'<' -f1", returnStdout: true).trim() + "-${env.TAG}"
                            }
                            sh "mvn -q -B versions:set -DnewVersion=${PREVIEW_VERSION} -DgenerateBackupPoms=false -T1C"
                            sh "mvn -q -B clean deploy -Dmaven.test.redirectTestOutputToFile=true -DskipITs -Djib.tag=${TAG} -Djib.repository=${LOCAL_REPOSITORY} -T1C"
                            // Fetch the target branch, sonar likes to take a look at it
                            sh "git fetch --no-tags origin ${CHANGE_TARGET}:refs/remotes/origin/${CHANGE_TARGET}"
                            sh "mvn -q -B sonar:sonar -Dsonar.login=${env.SONAR_TOKEN} -Dsonar.github.oauth=${env.GITHUB_TOKEN} -Dsonar.pullrequest.base=${CHANGE_TARGET} -Dsonar.pullrequest.branch=${BRANCH_NAME} -Dsonar.pullrequest.key=${env.CHANGE_ID} -Dsonar.pullrequest.provider=GitHub -Dsonar.pullrequest.github.repository=molgenis/molgenis -Dsonar.ws.timeout=120 -T1C"
                        }
                    }
                    post {
                        always {
                            junit '**/target/surefire-reports/**.xml'
                        }
                    }
                }
            }
        }
        stage('Steps [ master ]') {
            when {
                branch 'master'
            }
            environment {
                TAG = "dev-${TIMESTAMP}"
            }
            stages {
                stage('Build, Test, Push to Registries [ master ]') {
                    steps {
                        container('maven') {
                            sh "mvn -q -B clean deploy -Dmaven.test.redirectTestOutputToFile=true -DskipITs -Djib.tag=${TAG} -Djib.repository=${LOCAL_REPOSITORY} -Djib.to.tags=latest"
                            sh "mvn -q -B sonar:sonar -Dsonar.login=${SONAR_TOKEN} -Dsonar.ws.timeout=120"
                        }
                    }
                    post {
                        always {
                            junit '**/target/surefire-reports/**.xml'
                        }
                    }
                }
                stage("Deploy to dev [ master ]") {
                    steps {
                        milestone(ordinal: 100, label: 'deploy to master.dev.molgenis.org')
                        container('rancher') {
                            sh "rancher apps upgrade --set image.tag=${TAG} master ${CHART_VERSION}"
                        }
                    }
                }
            }
        }
        stage('Steps [ x.x ]') {
            when {
                expression { BRANCH_NAME ==~ /[0-9]\.[0-9]/ }
                beforeAgent true
            }
            agent {
                kubernetes {
                    inheritFrom 'shared'
                    // Build pod that includes backend services for integration tests
                    yamlFile ".jenkins/build-pod-it.yaml"
                }
            }
            stages {
                stage('Build test and publish [ x.x ]') {
                    steps {
                        dir("${JENKINS_AGENT_WORKDIR}/.m2") {
                            unstash 'maven-settings'
                        }
                        container('maven') {
                            sh "mvn -q -B clean deploy -Dmaven.test.redirectTestOutputToFile=true -DskipITs -Djib.tag=${BRANCH_NAME}-latest"
                            sh "mvn -q -B sonar:sonar -Dsonar.login=${SONAR_TOKEN} -Dsonar.branch.name=${BRANCH_NAME} -Dsonar.ws.timeout=120"
                        }
                    }
                }
                stage('Prepare Release [ x.x ]') {
                    steps {
                        timeout(time: 10, unit: 'MINUTES') {
                            input(message: 'Prepare to release?')
                        }
                        container('maven') {
                            sh "mvn -q -B verify -pl molgenis-api-tests -Dmaven.test.redirectTestOutputToFile=true"
                            sh "mvn -q -B verify -pl molgenis-platform-integration-tests -Dmaven.test.redirectTestOutputToFile=true"
                            sh "mvn -q -B release:prepare -DskipITs -Dmaven.test.redirectTestOutputToFile=true -Darguments=\"-q -B -DskipITs -Dmaven.test.redirectTestOutputToFile=true -Pproduction\""
                            script {
                                env.TAG = sh(script: "grep project.rel release.properties | head -n1 | cut -d'=' -f2", returnStdout: true).trim()
                            }
                        }
                    }
                }
                stage('Perform release [ x.x ]') {
                    steps {
                        container('maven') {
                            sh "mvn -q -B release:perform -Darguments=\"-q -B -Dwar.deploy.skip=false -DskipITs -Dmaven.test.redirectTestOutputToFile=true -Pproduction\""
                        }
                    }
                }
                stage('Deploy to test [ x.x ]') {
                    steps {
                        milestone(ordinal: 100, label: 'deploy to latest.test.molgenis.org')
                        dir("${JENKINS_AGENT_WORKDIR}/.rancher") {
                            unstash 'rancher-config'
                        }
                        container('rancher') {
                            sh "rancher context switch test-molgenis"
                            sh "rancher apps upgrade --set image.tag=${TAG} latest ${CHART_VERSION}"
                        }
                    }
                }
            }
        }
        stage('Steps [ feature ]') {
            when {
                expression { BRANCH_NAME ==~ /feature\/.*/ }
            }
            environment {
                TAG = "$BRANCH_NAME-$BUILD_NUMBER".replaceAll(~/[^\w.-]/, '-').toLowerCase()
            }
            stages {
                stage('Build, test and publish [ feature ]') {
                    steps {
                        container('maven') {
                            sh "mvn -q -B clean deploy -Dmaven.test.redirectTestOutputToFile=true -DskipITs -jib.tag=${TAG} -Djib.repository=${LOCAL_REPOSITORY}"
                            sh "mvn -q -B sonar:sonar -Dsonar.branch.name=${BRANCH_NAME} -Dsonar.login=${SONAR_TOKEN} -Dsonar.ws.timeout=120"
                        }
                    }
                    post {
                        always {
                            junit '**/target/surefire-reports/**.xml'
                        }
                    }
                }
            }
        }
    }
}
