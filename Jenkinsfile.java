def maven_artifact_repositories_json

node {
    configFileProvider( [configFile(fileId: 'maven-artifact-repositories', variable: 'maven_artifact_repositories')]) {
        script {
            maven_artifact_repositories_json = readFile maven_artifact_repositories
        }
    }
}

javarelease {
        artifactRepositoriesJSONDefaultValue = maven_artifact_repositories_json 
        gitRepositoryUrlDefaultValue = "${git_repo_url}"
}
