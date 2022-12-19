pipeline {
    agent any
    parameters {
        string(name: 'job_name', defaultValue: 'test-job', description: 'Name of the job being generated')
        string(name: 'artifact_repo_url', defaultValue: 'http://nexus/repository/maven-java-releases/', description: 'Repository where the artifacat will be published to')
    }
    stages {
        stage('Preprocess') {
            steps {
                script {
                    repository_branch_filter_regex = repository_full_name.concat('/refs/heads/master').replaceAll("/", "\\\\/")
                }
            }
        }
        stage('Create Job') {
            steps {
                jobDsl targets: 'pipelinejob.groovy', additionalParameters: [
                    job_name: job_name,
                    github_project_url: repository_clone_url,
                    repository_branch_filter_regex: repository_branch_filter_regex,
                    artifact_repo_url: artifact_repo_url,
                    artifact_repo_id: 'nexus',
                    git_repo_url: repository_clone_url
                ]
            }
        }
    }
}
