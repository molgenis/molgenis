pipeline {
    agent {
        kubernetes {
            label 'molgenis-maven'
        }
    }
    stages {
        stage('Run maven') {
            steps {
                container('molgenis-maven') {
                    // for PR: mvn clean package sonar:sonar -Dsonar.analysis.mode=preview -Dsonar.login=$SONAR_TOKEN -Dsonar.github.oauth=$GITHUB_TOKEN -Dsonar.github.pullRequest=$ghprbPullId -Dsonar.ws.timeout=120
                    sh 'mvn deploy -B -Dmaven.test.redirectTestOutputToFile=true -DskipTests'
                }
            }
        }
    }
}