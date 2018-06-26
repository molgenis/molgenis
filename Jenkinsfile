pipeline {
    agent {
        kubernetes {
            label 'molgenis-maven'
            yaml """
apiVersion: v1
kind: Pod
metadata:
  labels:
    kind: build-pod
    builder: maven
spec:
  containers:
  - name: maven
    image: webhost12.service.rug.nl/molgenis/molgenis-maven:latest
    command:
    - cat
    tty: true
    volumeMounts:
    - name: docker-sock
      mountPath: "/var/run/docker.sock"
  volumes:
    - name: docker-sock
      hostPath:
        path: "/var/run/docker.sock"
"""
        }
    }
    stages {
        stage('Run maven') {
            steps {
                container('maven') {
                    sh 'docker -v'
                    sh 'mvn verify --batch-mode --quiet -Dmaven.test.redirectTestOutputToFile=true -DskipITs -DskipTests'
                }
            }
        }
    }
}