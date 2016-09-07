def CONFIG_FILE = '/conf/lite-country-service.yaml'
def JAR_FILE = 'lite-country-service-1.0-SNAPSHOT.jar'
def IMAGE_NAME = '$LITE_DOCKER_REGISTRY/country-service'

node('jdk8') {
    
  sh "oc login https://ukbliteaom01.int.licensing.service.trade.gov.uk:8443 ${OC_CREDS} --insecure-skip-tls-verify=true"
  
  sh "oc project lite"
  
  stage 'Clean workspace'
  
  deleteDir()
  
  stage 'Checkout files'
  
  checkout scm
  
  stage 'Gradle build'
  
  sh 'chmod 777 gradlew'
  sh './gradlew build'
  
  step([$class: 'JUnitResultArchiver', testResults: 'build/test-results/**/*.xml'])
  
  stage 'OpenShift build'
  
  sh "oc start-build country-service --from-dir=."
}
