def maven_java_artifact_repositories_json

node {
    configFileProvider( [configFile(fileId: 'maven-java-artifact-repositories', variable: 'maven_java_artifact_repositories')]) {
        script {
            maven_java_artifact_repositories_json = readFile maven_java_artifact_repositories
        }
    }
}

javarelease {
        artifactRepositoriesJSONDefaultValue = maven_java_artifact_repositories_json 
        gitRepositoryUrlDefaultValue = "${git_repo_url}"
}
