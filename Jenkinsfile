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
    image: maven:3.5.4
    command:
    - cat
    tty: true
    volumeMounts:
    - name: docker-sock
      mountPath: "/var/run/docker.sock"
    - name: docker-exec
      mountPath: "/usr/bin/docker"
  volumes:
    - name: docker-sock
      hostPath:
        path: "/var/run/docker.sock"
    - name: docker-exec
      hostPath:
        path: "/usr/bin/docker"
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