pipeline {
    agent any
    stages {

        stage('Preprocess') {
            steps {
                script {
                    repository_branch_filter_regex = repository_full_name.concat("/refs/heads/${trunk_branch}").replaceAll("/", "\\\\/")
                }
            }
        }

        stage('Determine Project Type') {
            steps {
                checkout scmGit(
                                branches: [[name: "*/buildtest"]],
                                extensions: [
                                    cloneOption(depth: 1, noTags: true, reference: '', shallow: true),
                                    [$class: 'IgnoreNotifyCommit'],
                                    [$class: 'RelativeTargetDirectory', relativeTargetDir: "buildscripts"]
                                ],
                                userRemoteConfigs: [
                                    [credentialsId: 'jenkins_github_app', url:  'https://github.com/IreshMM/iresh-s-buildscripts.git']
                                ]
                         )
                checkout scmGit(
                                branches: [[name: "*/${trunk_branch}"]],
                                extensions: [
                                    cloneOption(depth: 1, noTags: true, reference: '', shallow: true),
                                    [$class: 'IgnoreNotifyCommit'],
                                    [$class: 'RelativeTargetDirectory', relativeTargetDir: repository_full_name]
                                ],
                                userRemoteConfigs: [
                                    [credentialsId: 'jenkins_github_app', url: repository_clone_url]
                                ]
                         )
                script {
                    project_type = sh(script: "./buildscripts/determineprojecttype.sh ${repository_full_name}", returnStdout: true)
                    if (project_type == 'UNDEFINED') error('Unable determine project type to be whether JAVA or ACE')
                }
            }
        }
                    
        stage('Create Job') {
            steps {
                jobDsl targets: 'pipelinejob.groovy', additionalParameters: [
                    job_name: job_name,
                    project_type: project_type,
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
