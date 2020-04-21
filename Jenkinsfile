pipeline {
  agent any
  stages {
    stage('GetDepends') {
      steps {
        sh 'git submodule update --init --recursive'
      }
    }

    stage('Build') {
      steps {
        sh 'bash ./gradlew build'
      }
    }

    stage('Archive') {
      steps {
        archiveArtifacts(artifacts: 'build/distributions/*.jar', fingerprint: true)
      }
    }

  }
}