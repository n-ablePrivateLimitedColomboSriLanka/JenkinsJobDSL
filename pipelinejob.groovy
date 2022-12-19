pipelineJob(job_name) {
  properties {
    githubProjectUrl('https://github.com/nable-integration-cicd-dev-mirror/JAVA_SAMPLE_SPRING.git')
    parameters {
      parameterDefinitions {
        string {
          name('release_branch')
          defaultValue('master')
        }
        string {
          name('artifact_repository_url')
          defaultValue('http://nexus:8081/repository/maven-testing/')
        }
        string {
          name('artifact_repository_id')
          defaultValue('nexus')
        }
        string {
          name('git_repository_url')
          defaultValue('https://github.com/nable-integration-cicd-dev-mirror/JAVA_SAMPLE_SPRING.git/')
        }
      }
      pipelineTriggers {
        triggers {
          GenericTrigger {
            genericVariables {
              genericVariable {
                key('reference')
                value('$.ref')
                expressionType('JSONPath')
              }
              genericVariable {
                key('repository_full_name')
                value('$.repository.full_name')
                expressionType('JSONPath')
              }
            }
            regexpFilterText('$repository_full_name/$reference')
            regexpFilterExpression('nable-integration-cicd-dev-mirror\\/JAVA_SAMPLE_SPRING\\/refs\\/heads\\/master')
            tokenCredentialId('generic_webhook_token')
          }
        }
      }
    }
  }
  definition {
      cps {
        script('''

          pipeline {
              agent any
              stages {
                  stage('Checkout SCM') {
                      steps {
                          cleanWs()
                          git credentialsId: 'jenkins_github_app', url: git_repository_url
                      }
                  }
                  stage('Deploy Artifact') {
                      steps {
                          withMaven(maven: 'maven3', mavenSettingsConfig: 'maven3-settings', publisherStrategy: 'EXPLICIT', options: [artifactsPublisher(disabled: false)]) {
                              sh "mvn clean package deploy -DaltDeploymentRepository=${artifact_repository_id}::default::${artifact_repository_url} -Dmaven.test.skip=true"
                          }
                      }
                  }
              }
          }
		'''
        )
      }
  }
}
