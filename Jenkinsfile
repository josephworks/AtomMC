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

    stage('DumpLibs') {
      steps {
        sh '''bash ./gradlew dumpLibs
zip -r libraries.zip build/libraries
mv libraries.zip build/libs'''
      }
    }

    stage('Archive') {
      steps {
        archiveArtifacts(artifacts: 'build/libs/*.jar', fingerprint: true)
      }
    }

  }
}