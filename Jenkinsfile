pipeline {
    agent {
        kubernetes {
            label 'molgenis-maven'
            containerTemplate {
                name 'maven'
                image 'maven:3.3.9'
                ttyEnabled true
                command 'cat'
            }
        }
    }
    stages {
        stage('Run maven') {
            steps {
                container('maven') {
                    sh 'mvn verify --batch-mode --quiet -DskipITs'
                }
            }
        }
    }
}