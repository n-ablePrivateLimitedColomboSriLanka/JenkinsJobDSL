import biz.nable.jenkins.github.GitHubOrgUtils

pipeline {
    agent any
    stages {
        stage('Preprocess') {
            steps {
                script {
                    repositories = []
                    if (x_github_event == 'none' || x_github_event == 'installation') {
                        if (params.repository_clone_url) {
                            clone_url_parts = params.repository_clone_url.split('/')
                            repository_name = clone_url_parts[4].split('\\.')[0]
                            repository = [
                                repository_full_name: "${clone_url_parts[3]}/${repository_name}",
                                repository_clone_url: params.repository_clone_url,
                                repository_name: repository_name
                            ]
                            repositories.add(repository)
                        } else {
                            gitHubOrgUtils = new GitHubOrgUtils('https://api.github.com', params.github_app_cred_id, params.github_org_name)
                            repositories = gitHubOrgUtils.getAllRepositories()
                        }
                    } else if (x_github_event == 'push') {
                        repository = [
                            repository_full_name: repository_full_name,
                            repository_clone_url: repository_clone_url,
                            repository_name: repository_name,
                        ]
                        repositories.add(repository)
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
                                branches: [[name: "*/master"]],
                                extensions: [
                                    cloneOption(depth: 1, noTags: true, reference: '', shallow: true),
                                    [$class: 'IgnoreNotifyCommit'],
                                    [$class: 'RelativeTargetDirectory', relativeTargetDir: "build_scripts"]
                                ],
                                userRemoteConfigs: [
                                    [credentialsId: params.github_app_cred_id, url: params.build_scripts_repo]
                                ]
                         )
            }
        }
                    

        stage('Determine Project Type') {
            steps {
                script {
                    processed_repos = []
                    for(repository in repositories) {
                        try {
                            checkout scmGit(
                                        branches: [[name: "*/${trunk_branch}"]],
                                        extensions: [
                                            cloneOption(depth: 1, noTags: true, reference: '', shallow: true),
                                            [$class: 'IgnoreNotifyCommit'],
                                            [$class: 'RelativeTargetDirectory', relativeTargetDir: repository['repository_full_name']]
                                        ],
                                        userRemoteConfigs: [
                                            [credentialsId: params.github_app_cred_id, url: repository['repository_clone_url']]
                                        ]
                                 )
                        } catch (err) {
                            echo 'Unable to clone the branch. Please check the whether branch exists!'
                            continue
                        }
                        project_type = sh(script: "./build_scripts/determineprojecttype.sh ${repository['repository_full_name']}", returnStdout: true).trim()
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
                            git_repo_url: repository['repository_clone_url'],
                            github_app_cred_id: params.github_app_cred_id,
                            build_scripts_repo: params.build_scripts_repo,
                            ace_shared_lib_index_url: params.ace_shared_lib_index_url,
                            trunk_branch: params.trunk_branch
                        ]

                    }
                }
            }
        }
    }
}
