def CONFIG_FILE = 'service.yaml'
def JAR_FILE = 'lite-country-service-1.0-SNAPSHOT.jar'
def IMAGE_NAME = '$LITE_DOCKER_REGISTRY/country-service'

stage 'dev-service-build'
node {
    deleteDir()
    git poll: true, changelog: true, credentialsId: "${GIT_CREDENTIALS}", url: "${GIT_URL}"
    sh 'chmod 777 gradlew'
    sh './gradlew build'

    step([$class: 'JUnitResultArchiver', testResults: 'build/test-results/**/*.xml'])
}

stage 'dev-docker-build'
node {
    withEnv(["IMAGE_NAME=${IMAGE_NAME}"]) {
        sh '/docker/docker build -t $IMAGE_NAME:$BUILD_NUMBER -t $IMAGE_NAME:latest .'
        sh '/docker/docker push $IMAGE_NAME'
    }
}
