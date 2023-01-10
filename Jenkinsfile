import biz.nable.jenkins.github.GitHubOrgUtils

pipeline {
    agent any
    stages {
        stage('Preprocess') {
            steps {
                script {
                    repositories = []
                    if(x_github_event == 'push') {
                        repository = [
                            repository_full_name: repository_full_name,
                            repository_clone_url: repository_clone_url,
                            repository_name: repository_name,
                        ]
                        repositories.add(repository)
                    } else if (x_github_event == 'installation') {
                        gitHubOrgUtils = new GitHubOrgUtils('https://api.github.com', 'jenkins_github_app', 'nable-integration-cicd-dev-mirror')
                        repositories = gitHubOrgUtils.getAllRepositories()
                    }

                    for(repository in repositories) {
                        repository['repository_branch_filter_regex'] = repository['repository_full_name']
                                                                        .concat("/refs/heads/${trunk_branch}")
                                                                        .replaceAll("/", "\\\\/")
                    }
                }
            }
        }

        stage('Checkout Build Scripts') {
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
            }
        }
                    

        stage('Determine Project Type') {
            steps {
                script {
                    processed_repos = []
                    for(repository in repositories) {
                        checkout scmGit(
                                    branches: [[name: "*/${trunk_branch}"]],
                                    extensions: [
                                        cloneOption(depth: 1, noTags: true, reference: '', shallow: true),
                                        [$class: 'IgnoreNotifyCommit'],
                                        [$class: 'RelativeTargetDirectory', relativeTargetDir: repository['repository_full_name']]
                                    ],
                                    userRemoteConfigs: [
                                        [credentialsId: 'jenkins_github_app', url: repository['repository_clone_url']]
                                    ]
                             )
                        project_type = sh(script: "./buildscripts/determineprojecttype.sh ${repository['repository_full_name']}", returnStdout: true).trim()
                        if (project_type == 'UNDEFINED') { 
                            echo 'Unable to determine project type to be whether JAVA or ACE'
                            continue
                        }
                        repository['project_type'] = project_type
                        processed_repos.add(repository)
                    }

                    repositories = processed_repos
                }
            }
        }
                    

        stage('Create Jobs') {
            steps {
                script {
                    for(repository in repositories) {
                        jobDsl targets: 'pipelinejob.groovy', additionalParameters: [
                            job_name: repository['repository_name'],
                            project_type: repository['project_type'],
                            github_project_url: repository['repository_clone_url'],
                            repository_branch_filter_regex: repository['repository_branch_filter_regex'],
                            artifact_repo_url: artifact_repo_url,
                            artifact_repo_id: 'nexus',
                            git_repo_url: repository['repository_clone_url']
                        ]

                    }
                }
            }
        }
    }
}
