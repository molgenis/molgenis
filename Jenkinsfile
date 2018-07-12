pipeline {
    agent {
        kubernetes {
            label 'molgenis'
        }
    }
    environment {
        ORG = 'molgenis'
        APP_NAME = 'molgenis-app'
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

        stage('Run integration test') {
            agent {
                kubernetes {
                    label 'molgenis-it'
                    defaultContainer 'jnlp'
                    yaml '''
apiVersion: v1
kind: Pod
metadata:
  labels:
    app: molgenis
spec:
  containers:
  #Java agent, test executor
  - name: jnlp
    image: registry.access.redhat.com/openshift3/jenkins-slave-maven-rhel7:v3.9
    command:
    - /bin/sh
    args:
    - -c
    - umask 0000; /usr/local/bin/run-jnlp-client $(JENKINS_SECRET) $(JENKINS_NAME)
    resources:
      limits:
        memory: 512Mi
    workingDir: /home/jenkins
    env:
    - name: JNLP_MAX_HEAP_UPPER_BOUND_MB
      value: 64
  #App under test
  - name: molgenis
    image: registry.molgenis.org/molgenis/molgenis-app:latest
    resources:
      limits:
        memory: 512Mi
    env:
    - name: SPRING_PROFILES_ACTIVE
      value: k8sit
    - name: SPRING_CLOUD_KUBERNETES_ENABLED
      value: false
  #DB
  - name: mariadb
    image: registry.access.redhat.com/rhscl/mariadb-102-rhel7:1
    resources:
      limits:
        memory: 256Mi
    env:
    - name: MYSQL_USER
      value: myuser
    - name: MYSQL_PASSWORD
      value: mypassword
    - name: MYSQL_DATABASE
      value: testdb
    - name: MYSQL_ROOT_PASSWORD
      value: secret
    readinessProbe:
      tcpSocket:
        port: 3306
      initialDelaySeconds: 5
  #AMQ
  - name: amq
    image: registry.access.redhat.com/jboss-amq-6/amq63-openshift:1.3
    resources:
      limits:
        memory: 256Mi
    env:
    - name: AMQ_USER
      value: test
    - name: AMQ_PASSWORD
      value: secret
    readinessProbe:
      tcpSocket:
        port: 61616
      initialDelaySeconds: 5
  #External API Third party (provided by mockserver)
  - name: mockserver
    image: jamesdbloom/mockserver:mockserver-5.3.0
    resources:
      limits:
        memory: 256Mi
    env:
    - name: LOG_LEVEL
      value: INFO
    - name: JVM_OPTIONS
      value: -Xmx128m
    readinessProbe:
      tcpSocket:
        port: 1080
      initialDelaySeconds: 5
'''
                }
            }
            environment {
                //These env vars are used the tests to send message to users.in queue
                AMQ_USER = 'test'
                AMQ_PASSWORD = 'secret'
            }
            steps {
                dir("integration-test") {
                    container('mariadb') {
                        //requires mysql
                        sh 'sql/setup.sh'
                    }

                    // Default container 'jnlp'
                    // this script requires curl and python.
                    sh 'mockserver/setup.sh'

                    //Run the tests.
                    //Somehow simply "mvn ..." doesn't work here
                    sh '/bin/bash -c "mvn -s ../configuration/settings.xml -B clean test"'
                }
            }
            post {
                always {
                    junit testResults: 'integration-test/target/surefire-reports/*.xml', allowEmptyResults: true
                }
            }
        }

    }

}
}
