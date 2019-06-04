pipeline {
    agent {
        kubernetes {
            label 'molgenis'
        }
    }
    environment {
        LOCAL_REPOSITORY = "${LOCAL_REGISTRY}/molgenis/molgenis-app"
        CHART_VERSION = '0.12.0'
    }
    stages {
        stage('Retrieve build secrets') {
            steps {
                container('vault') {
                    script {
                        sh "mkdir /home/jenkins/.m2"
                        sh "mkdir /home/jenkins/.rancher"
                        sh(script: 'vault read -field=value secret/ops/jenkins/rancher/cli2.json > /home/jenkins/.rancher/cli2.json')
                        sh(script: 'vault read -field=value secret/ops/jenkins/maven/settings.xml > /home/jenkins/.m2/settings.xml')
                        env.SONAR_TOKEN = sh(script: 'vault read -field=value secret/ops/token/sonar', returnStdout: true)
                        env.GITHUB_TOKEN = sh(script: 'vault read -field=value secret/ops/token/github', returnStdout: true)
                        env.PGP_PASSPHRASE = 'literal:' + sh(script: 'vault read -field=passphrase secret/ops/certificate/pgp/molgenis-ci', returnStdout: true)
                        env.CODECOV_TOKEN = sh(script: 'vault read -field=value secret/ops/token/codecov', returnStdout: true)
                        env.GITHUB_USER = sh(script: 'vault read -field=username secret/ops/token/github', returnStdout: true)
                    }
                }
                dir('/home/jenkins/.m2') {
                    stash includes: 'settings.xml', name: 'maven-settings'
                }
                dir('/home/jenkins/.rancher') {
                    stash includes: 'cli2.json', name: 'rancher-config'
                }
            }
        }
        stage('Steps [ PR ]') {
            when {
                changeRequest()
            }
            environment {
                // PR-1234-231
                TAG = "PR-${CHANGE_ID}-${BUILD_NUMBER}"
                // 0.0.0-SNAPSHOT-PR-1234-231
                PREVIEW_VERSION = "0.0.0-SNAPSHOT-${TAG}"
            }
            stages {
                stage('Build [ PR ]') {
                    steps {
                        container('maven') {
                            sh "mvn -q -B versions:set -DnewVersion=${PREVIEW_VERSION} -DgenerateBackupPoms=false"
                            sh "mvn -q -B clean verify -Dmaven.test.redirectTestOutputToFile=true -DskipITs -T4"
                            sh "curl -s https://codecov.io/bash | bash -s - -c -F unit -K -C ${GIT_COMMIT}"
                            sh "mvn -q -B sonar:sonar -Dsonar.login=${env.SONAR_TOKEN} -Dsonar.github.oauth=${env.GITHUB_TOKEN} -Dsonar.pullrequest.base=${CHANGE_TARGET} -Dsonar.pullrequest.branch=${BRANCH_NAME} -Dsonar.pullrequest.key=${env.CHANGE_ID} -Dsonar.pullrequest.provider=GitHub -Dsonar.pullrequest.github.repository=molgenis/molgenis -Dsonar.ws.timeout=120"
                            dir('molgenis-app'){
                                sh "mvn -q -B dockerfile:build dockerfile:tag dockerfile:push -Ddockerfile.tag=${TAG} -Ddockerfile.repository=${LOCAL_REPOSITORY}"
                            }
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
                TAG = "dev-$BUILD_NUMBER"
            }
            stages {
                stage('Build [ master ]') {
                    steps {
                        container('maven') {
                            sh "mvn -q -B clean verify -Dmaven.test.redirectTestOutputToFile=true -DskipITs -T4"
                            sh "curl -s https://codecov.io/bash | bash -s - -c -F unit -K  -C ${GIT_COMMIT}"
                            sh "mvn -q -B sonar:sonar -Dsonar.login=${SONAR_TOKEN} -Dsonar.ws.timeout=120"
                            dir('molgenis-app'){
                                sh "mvn -q -B dockerfile:build dockerfile:tag dockerfile:push -Ddockerfile.tag=${TAG} -Ddockerfile.repository=${LOCAL_REPOSITORY}"
                                sh "mvn -q -B dockerfile:tag dockerfile:push -Ddockerfile.tag=dev -Ddockerfile.repository=${LOCAL_REPOSITORY}"
                            }
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
                            sh "rancher context switch dev-molgenis"
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
                    label('molgenis-it')
                }
            }
            stages {
                stage('Build [ x.x ]') {
                    steps {
                        dir('/home/jenkins/.m2') {
                            unstash 'maven-settings'
                        }
                        container('maven') {
                            sh "mvn -q -B clean install -Dmaven.test.redirectTestOutputToFile=true -DskipITs -T4"
                            sh "curl -s https://codecov.io/bash | bash -s - -c -F unit -K  -C ${GIT_COMMIT}"
                            sh "mvn -q -B sonar:sonar -Dsonar.login=${SONAR_TOKEN} -Dsonar.branch.name=${BRANCH_NAME} -Dsonar.ws.timeout=120"
                            dir('molgenis-app'){
                                sh "mvn -q -B dockerfile:build dockerfile:tag dockerfile:push -Ddockerfile.tag=${BRANCH_NAME}-latest"
                                sh "mvn -q -B dockerfile:tag dockerfile:push -Ddockerfile.tag=latest"
                            }
                        }
                    }
                }
                stage('Prepare Release [ x.x ]') {
                    steps {
                        timeout(time: 10, unit: 'MINUTES') {
                            input(message: 'Prepare to release?')
                        }
                        container('maven') {
                            sh "mvn -q -B verify -pl molgenis-platform-integration-tests -Dmaven.test.redirectTestOutputToFile=true -Dit_db_user=molgenis -Dit_db_password=molgenis -Dit_db_name=molgenis -Delasticsearch.cluster.name=molgenis -Delasticsearch.transport.addresses=localhost:9300 -P!create-it-db -P!create-it-es"
                            sh "mvn -q -B release:prepare -DskipITs -Dmaven.test.redirectTestOutputToFile=true -Darguments=\"-q -B -DskipITs -Dmaven.test.redirectTestOutputToFile=true -Pproduction\""
                            script {
                                env.TAG = sh(script: "grep project.rel release.properties | head -n1 | cut -d'=' -f2", returnStdout: true).trim()
                            }
                            dir('molgenis-app') {
                                sh "mvn -q -B dockerfile:build dockerfile:tag dockerfile:push -Ddockerfile.tag=${TAG} -Ddockerfile.repository=${LOCAL_REPOSITORY} -Ddockerfile.warfile.version=${TAG}"
                            }
                        }
                    }
                }
                stage('Deploy to test [ x.x ]') {
                    steps {
                        milestone(ordinal: 100, label: 'deploy to latest.test.molgenis.org')
                        dir('/home/jenkins/.rancher') {
                            unstash 'rancher-config'
                        }
                        container('rancher') {
                            sh "rancher context switch test-molgenis"
                            sh "rancher apps upgrade --set image.tag=${TAG} latest ${CHART_VERSION}"
                        }
                    }
                }
                stage('Manual test [ x.x ]') {
                    steps {
                        input(message: 'Ok to release?')
                    }
                }
                stage('Perform release [ x.x ]') {
                    steps {
                        container('vault') {
                            script {
                                env.PGP_SECRETKEY = "keyfile:/home/jenkins/key.asc"
                                sh(script: 'vault read -field=secret.asc secret/ops/certificate/pgp/molgenis-ci > /home/jenkins/key.asc')
                            }
                        }
                        container('maven') {
                            sh "mvn -B release:perform -Darguments=\"-B -DskipTests -Pproduction\""
                            // Can not use DSL here because of bug in Jenkins
                            // The build wants to create a tmp directory in the target/checkout/molgenis-app
                            // This is not permitted
                            sh "cd target/checkout/molgenis-app && mvn -q -B dockerfile:build dockerfile:tag dockerfile:push -Ddockerfile.tag=${TAG}"
                            sh "cd target/checkout/molgenis-app && mvn -q -B dockerfile:tag dockerfile:push -Ddockerfile.tag=${BRANCH_NAME}-stable"
                            sh "cd target/checkout/molgenis-app && mvn -q -B dockerfile:tag dockerfile:push -Ddockerfile.tag=stable"
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
                stage('Build [ feature ]') {
                    steps {
                        container('maven') {
                            sh "mvn -q -B clean verify -Dmaven.test.redirectTestOutputToFile=true -DskipITs"
                            sh "curl -s https://codecov.io/bash | bash -s - -c -F unit -K  -C ${GIT_COMMIT}"
                            sh "mvn -q -B sonar:sonar -Dsonar.branch.name=${BRANCH_NAME} -Dsonar.login=${SONAR_TOKEN} -Dsonar.ws.timeout=120"
                            dir('molgenis-app'){
                                sh "mvn -q -B dockerfile:build dockerfile:tag dockerfile:push -Ddockerfile.tag=${TAG} -Ddockerfile.repository=${LOCAL_REPOSITORY}"
                            }
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
