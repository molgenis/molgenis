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
                    sh 'docker -v'
                    sh 'mvn verify --batch-mode --quiet -Dmaven.test.redirectTestOutputToFile=true -DskipITs -DskipTests'
                }
            }
        }
    }
}