import groovy.text.SimpleTemplateEngine

def fileContents = readFileFromWorkspace "Jenkinsfile.${project_type.toLowerCase().trim()}"

def engine = new SimpleTemplateEngine()
template = engine.createTemplate(fileContents).make(binding.getVariables()).toString()

folder(job_name) {
    displayName(job_name)
    description(job_name)
}

pipelineJob("${job_name}/Release") {
  properties {
    githubProjectUrl(github_project_url)
    parameters {
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
            regexpFilterExpression(repository_branch_filter_regex)
            tokenCredentialId('generic_webhook_token')
          }
        }
      }
    }
  }
  definition {
      cps {
        script(template)
        sandbox()
      }
  }
}
