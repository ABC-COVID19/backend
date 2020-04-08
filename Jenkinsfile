pipeline {
    agent {
        kubernetes {
            yamlFile 'k8s/jenkins-slave.yaml'
            defaultContainer 'java11'
        }
    } 

    triggers {
        pollSCM('H/2 * * * *')
    }

    options {
        disableConcurrentBuilds()
        timeout(time: 10)
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }

    environment {
        DEPLOYMENT_NAME = "api-backend" 
        GIT_REPO = "github.com/ABC-COVID19/API-backend.git"
        NAMESPACE_DEV = "icam-dev" 
        NAMESPACE_PROD = "icam-prod" 
        DOCKER_HUB = "docker.icam.org.pt" //Need refactor to dns name
        // SLACK_CHANNEL = '' 
        // SLACK_TEAM_DOMAIN = ''
        // SLACK_TOKEN = credentials('')
        //PROJECT_NAME = readMavenPom().getArtifactId()
        PROJECT_VERSION = readMavenPom().getVersion()
        GIT_USER = 'jenkins-icam@protonmail.com'
        GIT_USER_NAME = 'jenkins-icam'
        //NEW_VERSION = chooseVersion("${PROJECT_VERSION}","${env.GIT_BRANCH}")
        DEBUG_MODE = '-q' // "-q" (quiet)  "-X" (verbose) 

    }

    stages {

        // stage('Recursive build Check') {
        //     steps {
        //         script {
        //             if (checkCommit("updated pom version to")){
        //                 timeout(time: 60, unit: 'SECONDS') {
        //                     input 'Do you want to Update Version anyway?'
        //                 }
        //             }
        //         }
        //     }
        // }

        stage('Build and Test') {
            steps {
                container('java11'){
                    sh 'unset MAVEN_CONFIG'
                    sh "./mvnw package verify jib:dockerBuild"
                    sh "docker tag icamapi:latest ${DOCKER_HUB}/${DEPLOYMENT_NAME}:${PROJECT_VERSION}"
                }
            }
        }

        // stage('Build Docker Image') {
        //     steps {
        //         container('java11'){
        //         }
        //     }
        // }
		
        stage('Merge to Develop') {
            when {
                branch "feature*"
            }
             steps {
                        sh "git config --global user.email '${GIT_USER}'"
                        sh "git config --global user.name '${GIT_USER_NAME}'"
                        sh "git checkout -f origin/develop" 
                        sh "git merge --ff ${env.GIT_COMMIT}"

                        withCredentials([usernamePassword(credentialsId: 'Jenkins-ICAM2', usernameVariable: 'username', passwordVariable: 'password')]) {
                            //sh "ssh-keyscan -t rsa ${GIT_HOST} >> ~/.ssh/known_hosts"
                            //sh "ssh-agent bash -c 'ssh-add ${GIT_CREDS}; 
                             sh "git push https://${username}:${password}@${GIT_REPO} HEAD:develop'"
                        }
            }
        }

        stage('Merge to Master') {
            when {
                branch "develop"
            }
            steps {
                script{
                    try{
                        timeout(time: 90, unit: 'SECONDS') {
                            input 'Confirm Merge to master?'
                        }

                        sh "git config --global user.email '${GIT_USER}'"
                        sh "git config --global user.name '${GIT_USER_NAME}'"
                        sh "git checkout -f origin/master" 
                        sh "git merge --ff ${env.GIT_COMMIT}"


                        withCredentials([usernamePassword(credentialsId: 'Jenkins-ICAM2', usernameVariable: 'username', passwordVariable: 'password')]) {
                            //sh "ssh-keyscan -t rsa ${GIT_HOST} >> ~/.ssh/known_hosts"
                            //sh "ssh-agent bash -c 'ssh-add ${GIT_CREDS};
                            sh "git push https://${username}:${password}@${GIT_REPO} HEAD:master'"

                        }
                    }catch(err){
                        sh "echo Skipped by users"
                    }
                }
            }
        }
        
        stage('Deliver to Hub - Deploy to DEV') {
            when {
                branch "develop"
            }
            steps {
                container('az-kube'){
                        sh "docker tag ${DOCKER_HUB}/${DEPLOYMENT_NAME}:${PROJECT_VERSION} ${DOCKER_HUB}/${DEPLOYMENT_NAME}:latest"
                        sh "docker push ${DOCKER_HUB}/${DEPLOYMENT_NAME}:${PROJECT_VERSION}"
                        sh "docker push ${DOCKER_HUB}/${DEPLOYMENT_NAME}:latest"
                        withCredentials([azureServicePrincipal('Azure_login')]) {
                                    sh "az login --service-principal -u ${AZURE_CLIENT_ID} -p ${AZURE_CLIENT_SECRET} -t ${AZURE_TENANT_ID}"
                                    sh "az aks get-credentials --name icam --resource-group icam --overwrite-existing"
                                    sh "kubectl set image deployment ${DEPLOYMENT_NAME} ${DEPLOYMENT_NAME}=${DOCKER_HUB}/${DEPLOYMENT_NAME}:${PROJECT_VERSION} --record -n ${NAMESPACE_DEV}"
                                }
                }
            }
        }
        stage('Deliver to Hub - Deploy to PROD') {
            when {
                branch "master"
            }
            steps {
                container('az-kube'){
                        sh "docker tag ${DOCKER_HUB}/${DEPLOYMENT_NAME}:${PROJECT_VERSION} ${DOCKER_HUB}/${DEPLOYMENT_NAME}:latest"
                        sh "docker push ${DOCKER_HUB}/${DEPLOYMENT_NAME}:${PROJECT_VERSION}"
                        sh "docker push ${DOCKER_HUB}/${DEPLOYMENT_NAME}:latest"

                        withCredentials([azureServicePrincipal('Azure_login')]) { 
                                    sh "az login --service-principal -u ${AZURE_CLIENT_ID} -p ${AZURE_CLIENT_SECRET} -t ${AZURE_TENANT_ID}"
                                    sh "az aks get-credentials --name icam --resource-group icam --overwrite-existing"
                                    sh "kubectl set image deployment ${DEPLOYMENT_NAME} ${DEPLOYMENT_NAME}=${DOCKER_HUB}/${DEPLOYMENT_NAME}:${PROJECT_VERSION} --record -n ${NAMESPACE_PROD}"
                                }



                }
            }
        }
    }

    post {
        always {
            container('az-kube'){
                sh "docker rmi ${DOCKER_HUB}/${DEPLOYMENT_NAME}:${PROJECT_VERSION} || true "
                sh "docker rmi ${DOCKER_HUB}/${DEPLOYMENT_NAME}:latest || true "
            }
            // notifySlack()
        }
        cleanup {
            cleanWs()
        }
    }
}

/*****************************************
 * To use this function you need to install
 * the Slack Notification Plugin
 ****************************************/

def notifySlack(additionalInfo = '') {
    def colorCode = '#79ae40'
    def status = 'SUCCESS'
    if (currentBuild.result == 'FAILURE') {
        colorCode = '#d34e56'
        status = 'FAILURE'
    }
    def commitText = sh(returnStdout: true, script: 'git show -s --format=format:"*%s*  _by %an_" HEAD').trim()
    def subject = "${env.JOB_NAME} - #${env.BUILD_NUMBER} ${status} (<${env.BUILD_URL}|Open>)"
    def summary = "${subject}\nChanges: ${commitText}\nBranch: ${env.GIT_BRANCH}\n${additionalInfo}"
    slackSend channel: "${env.SLACK_CHANNEL}", color: colorCode, message: summary, teamDomain: "${env.SLACK_TEAM_DOMAIN}", token: "${env.SLACK_TOKEN}"
}

def getLastSprint(branch){
    check = sh(returnStdout: true, script: 'git branch -a | grep sprint | tail -1 | cut -d "/" -f 3')
    return check.contains(branch)
}

def chooseVersion(oldVersion, branch){
    return branch.contains("sprint_") ? bumpVersion(oldVersion,'patch') : 
            branch.contains("develop") ? bumpVersion(oldVersion,'minor') : "NONE"
}

def bumpVersion(oldVersion, arg) {
    //eg: bumpVersion(0.2.3-Snapshot, major) -> 1.0.0-Snapshot 
    def pos = ["major", "minor", "patch"].indexOf(arg)
    def parts = oldVersion.split("-|\\.")
    parts[pos] = Integer.parseInt(parts[pos]) + 1
    for(i = pos + 1; i < 3; i++) parts[i] = 0
    return parts.length == 3 ? parts[0..2].join('.') : [parts[0..2].join('.'), parts[3]].join('-')
}

def checkCommit(message) {
    def commitText = sh(returnStdout: true, script: 'git show -s --format="%s"').trim()
    return commitText.contains(message)
}