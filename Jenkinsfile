pipeline {
    agent {
        kubernetes {
            label 'molgenis-it'
        }
    }
    environment {
        npm_config_registry = "http://nexus.molgenis-nexus:8081/repository/npm-central/"
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
                    sh "mvn install -DskipTests -Dmaven.javadoc.skip=true -B -V -T4 -Ddockerfile.tag=${TAG} -Ddockerfile.skip=false"
                }

                parallel {
                    stage('unit test'){
                        steps {
                            container('maven') {
                                sh "mvn test -Dmaven.test.redirectTestOutputToFile=true -DskipITs"
                            }
                        }
                    }
                    stage('integration test') {
                        steps {
                            container('maven') {
                                sh "mvn verify -pl molgenis-platform-integration-tests --batch-mode --quiet -Dmaven.test.redirectTestOutputToFile=true -Dit_db_user=postgres -Dit_db_password -Dit_db_name=molgenis -Delasticsearch.cluster.name=molgenis -Delasticsearch.transport.addresses=localhost:9300 -P!create-it-db -P!create-it-es"
                            }
                        }
                    }
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
    }
}
