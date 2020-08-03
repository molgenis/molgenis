pipeline {
    agent {
        kubernetes {
            label 'molgenis-jdk11'
        }
    }
    environment {
        LOCAL_REPOSITORY = "${LOCAL_REGISTRY}/molgenis/molgenis-app"
        YUM_REPOSITORY_SNAPSHOTS = "https://${env.LOCAL_REGISTRY}/repository/yum-snapshots/"
        YUM_REPOSITORY_RELEASES = "https://${env.LOCAL_REGISTRY}/repository/yum-releases/"
        CHART_VERSION = '1.10.0'
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
                        env.PGP_PASSPHRASE = 'literal:' + sh(script: 'vault read -field=passphrase secret/ops/certificate/pgp/molgenis-ci', returnStdout: true)
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
                // PR-1234-231
                TAG = "PR-${CHANGE_ID}-${BUILD_NUMBER}"
            }
            stages {
                stage('Build [ PR ]') {
                    steps {
                        container('maven') {
                            script {
                                env.PREVIEW_VERSION = sh(script: "grep version pom.xml | grep SNAPSHOT | cut -d'>' -f2 | cut -d'<' -f1", returnStdout: true).trim() + "-${env.TAG}"
                            }
                            sh "mvn -q -B versions:set -DnewVersion=${PREVIEW_VERSION} -DgenerateBackupPoms=false -T1C"
                            sh "mvn -q -B clean install -Dmaven.test.redirectTestOutputToFile=true -DskipITs -T1C -DargLine='-XX:TieredStopAtLevel=1 -noverify'"
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
                stage('Push to registries [ PR ]') {
                    steps {
                        container('maven') {
                            dir('molgenis-app') {
                                script {
                                    sh "mvn -q -B dockerfile:build dockerfile:tag dockerfile:push -Ddockerfile.tag=${TAG} -Ddockerfile.repository=${LOCAL_REPOSITORY} -T1C"
                                    sh "mvn -q -B rpm:rpm -Drpm.release.version=${env.PREVIEW_VERSION} -T1C"
                                    // make sure you have no linebreaks in RPM variable
                                    env.RPM = sh(script: 'ls -1 target/rpm/molgenis/RPMS/noarch', returnStdout: true).trim()
                                    sh "mvn deploy:deploy-file -DartifactId=molgenis -DgroupId=org.molgenis -Dversion=${env.PREVIEW_VERSION} -DrepositoryId=${env.LOCAL_REGISTRY} -Durl=${YUM_REPOSITORY_SNAPSHOTS} -Dfile=target/rpm/molgenis/RPMS/noarch/${env.RPM} -T1C"
                                }
                            }
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
                stage('Build [ master ]') {
                    steps {
                        container('maven') {
                            sh "mvn -q -B clean install -Dmaven.test.redirectTestOutputToFile=true -DskipITs"
                            sh "mvn -q -B sonar:sonar -Dsonar.login=${SONAR_TOKEN} -Dsonar.ws.timeout=120"
                        }
                    }
                    post {
                        always {
                            junit '**/target/surefire-reports/**.xml'
                        }
                    }
                }
                stage('Push to registries [ master ]') {
                    steps {
                        container('maven') {
                            dir('molgenis-app') {
                                script {
                                    sh "mvn -q -B dockerfile:build dockerfile:tag dockerfile:push -Ddockerfile.tag=${TAG} -Ddockerfile.repository=${LOCAL_REPOSITORY}"
                                    sh "mvn -q -B dockerfile:tag dockerfile:push -Ddockerfile.tag=dev -Ddockerfile.repository=${LOCAL_REPOSITORY}"
                                    env.RPM_TAG = sh(script: 'mvn help:evaluate -Dexpression=project.version -q -DforceStdout', returnStdout: true)
                                    sh "mvn -q -B rpm:rpm -Drpm.release.version=${RPM_TAG}"
                                    // make sure you have no linebreaks in RPM variable
                                    env.RPM = sh(script: 'ls -1 target/rpm/molgenis/RPMS/noarch', returnStdout: true).trim()
                                    sh "mvn deploy:deploy-file -DartifactId=molgenis -DgroupId=org.molgenis -Dversion=${env.TAG} -DrepositoryId=${env.LOCAL_REGISTRY} -Durl=${YUM_REPOSITORY_SNAPSHOTS} -Dfile=target/rpm/molgenis/RPMS/noarch/${env.RPM}"
                                }
                            }
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
                    label('molgenis-it-jdk11')
                }
            }
            stages {
                stage('Build [ x.x ]') {
                    steps {
                        dir("${JENKINS_AGENT_WORKDIR}/.m2") {
                            unstash 'maven-settings'
                        }
                        container('maven') {
                            sh "mvn -q -B clean install -Dmaven.test.redirectTestOutputToFile=true -DskipITs"
                            sh "mvn -q -B sonar:sonar -Dsonar.login=${SONAR_TOKEN} -Dsonar.branch.name=${BRANCH_NAME} -Dsonar.ws.timeout=120"
                            dir('molgenis-app') {
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
                        }
                    }
                }
                stage('Push release candidates to registries [ x.x ]') {
                    steps {
                        container('maven') {
                            script {
                                env.TAG = sh(script: "grep project.rel release.properties | head -n1 | cut -d'=' -f2", returnStdout: true).trim()
                            }
                            // deploy Docker image
                            dir('molgenis-app') {
                                script {
                                    sh "mvn -q -B dockerfile:build dockerfile:tag dockerfile:push -Ddockerfile.tag=${TAG} -Ddockerfile.repository=${LOCAL_REPOSITORY} -Ddockerfile.warfile.version=${TAG}"
                                }
                            }
                            // deploy RPM
                            // need to run install phase first
                            // the rpm:rpm goal is bound to the package phase
                            // which implies that the next snapshot version is installed
                            // the artifact is built and the rpm plugin refers to the artifact build in the release:prepare goal
                            sh "mvn -q -B install -DskipTests"
                            dir('molgenis-app') {
                                script {
                                    sh "mvn -q -B rpm:rpm -Drpm.release.version=${TAG}"
                                    // make sure you have no linebreaks in RPM variable
                                    env.RPM = sh(script: 'ls -1 target/rpm/molgenis/RPMS/noarch', returnStdout: true).trim()
                                    sh "mvn deploy:deploy-file -DartifactId=molgenis -DgroupId=org.molgenis -Dversion=${env.TAG} -DrepositoryId=${env.LOCAL_REGISTRY} -Durl=${YUM_REPOSITORY_SNAPSHOTS} -Dfile=target/rpm/molgenis/RPMS/noarch/${env.RPM}"
                                }
                            }
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
                stage('Manual test [ x.x ]') {
                    steps {
                        input(message: 'Ok to release?')
                    }
                }
                stage('Perform release [ x.x ]') {
                    steps {
                        container('vault') {
                            script {
                                env.PGP_SECRETKEY = "keyfile:${JENKINS_AGENT_WORKDIR}/key.asc"
                                sh(script: "vault read -field=secret.asc secret/ops/certificate/pgp/molgenis-ci > ${JENKINS_AGENT_WORKDIR}/key.asc")
                            }
                        }
                        container('maven') {
                            sh "mvn -q -B release:perform -Darguments=\"-q -B -DskipITs -Dmaven.test.redirectTestOutputToFile=true -Pproduction\""
                        }
                    }
                }
                stage('Push to registries [ x.x ]') {
                    steps {
                        container('maven') {
                            script {
                                // Can not use DSL here because of bug in Jenkins
                                // The build wants to create a tmp directory in the target/checkout/molgenis-app
                                // This is not permitted
                                sh "cd target/checkout/molgenis-app && mvn -q -B dockerfile:build dockerfile:tag dockerfile:push -Ddockerfile.tag=${TAG}"
                                sh "cd target/checkout/molgenis-app && mvn -q -B dockerfile:tag dockerfile:push -Ddockerfile.tag=${BRANCH_NAME}-stable"
                                sh "cd target/checkout/molgenis-app && mvn -q -B dockerfile:tag dockerfile:push -Ddockerfile.tag=stable"
                                // Build RPM to push to registry
                                sh "cd target/checkout/molgenis-app && mvn -q -B rpm:rpm -Drpm.release.version=${TAG}"
                                // make sure you have no linebreaks in RPM variable
                                env.RPM = sh(script: 'cd target/checkout/molgenis-app && ls -1 target/rpm/molgenis/RPMS/noarch', returnStdout: true).trim()
                                sh "cd target/checkout/molgenis-app && mvn deploy:deploy-file -DartifactId=molgenis -DgroupId=org.molgenis -Dversion=${env.TAG} -DrepositoryId=${env.LOCAL_REGISTRY} -Durl=${YUM_REPOSITORY_RELEASES} -Dfile=target/rpm/molgenis/RPMS/noarch/${env.RPM}"
                            }
                        }
                    }
                }
                stage('Manually close and release on sonatype [ x.x ]') {
                    steps {
                        input(message='Log on to https://oss.sonatype.org/ and manually close and release to maven central.')
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
                            sh "mvn -q -B clean install -Dmaven.test.redirectTestOutputToFile=true -DskipITs"
                            sh "mvn -q -B sonar:sonar -Dsonar.branch.name=${BRANCH_NAME} -Dsonar.login=${SONAR_TOKEN} -Dsonar.ws.timeout=120"
                        }
                    }
                    post {
                        always {
                            junit '**/target/surefire-reports/**.xml'
                        }
                    }
                }
                stage('Push to registries [ feature ]') {
                    steps {
                        container('maven') {
                            dir('molgenis-app') {
                                script {
                                    sh "mvn -q -B dockerfile:build dockerfile:tag dockerfile:push -Ddockerfile.tag=${TAG} -Ddockerfile.repository=${LOCAL_REPOSITORY}"
                                    sh "mvn -q -B rpm:rpm -Drpm.version=${TAG}"
                                    // make sure you have no linebreaks in RPM variable
                                    env.RPM = sh(script: 'ls -1 target/rpm/molgenis/RPMS/noarch', returnStdout: true).trim()
                                    sh "mvn deploy:deploy-file -DartifactId=molgenis -DgroupId=org.molgenis -Dversion=${env.TAG} -DrepositoryId=${env.LOCAL_REGISTRY} -Durl=${YUM_REPOSITORY_SNAPSHOTS} -Dfile=target/rpm/molgenis/RPMS/noarch/${env.RPM}"
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
