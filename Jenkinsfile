pipeline {
    agent {
        kubernetes {
            label 'molgenis'
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

        stage('PR') {
            parallel {
                stage('unit test and publish docker container'){
                    container('maven') {
                        environment {
                            TAG = "PR-${CHANGE_ID}-${BUILD_NUMBER}"
                        }
                        steps {
                            sh "mvn install -Dmaven.test.redirectTestOutputToFile=true -DskipITs -Ddockerfile.tag=${TAG} -Ddockerfile.skip=false"
                        }
                    }
                }
                stage('integration test') {
                    agent {
                        kubernetes {
                            label 'molgenis-it'
                        }
                    }
                    steps {
                        container('maven') {
                            sh "mvn install -DskipTests -Dmaven.javadoc.skip=true -B -V -T4"
                            sh "mvn verify -pl molgenis-platform-integration-tests --batch-mode --quiet -Dmaven.test.redirectTestOutputToFile=true -Dit_db_user=postgres -Dit_db_password -P!create-it-db"
                        }
                    }
                }
                stage('api test') {
                    agent {
                        kubernetes {
                            label 'molgenis-it'
                        }
                    }
                    steps {
                        container('maven') {
                            sh "mvn install -DskipTests -Dmaven.javadoc.skip=true -B -V -T4"
                            sh "mvn verify -pl molgenis-api-tests --batch-mode --quiet -Dmaven.test.redirectTestOutputToFile=true -Dit_db_user=postgres -Dit_db_password -P!create-it-db"
                        }
                    }
                }
            }
        }

    }
}
