stage 'Load files from GitHub'
def environment, helloworld
fileLoader.withSVN('https://github.com/jenkinsci/workflow-remote-loader-plugin/trunk/examples@HEAD') {
    helloworld = fileLoader.load('fileLoader/helloworld');
    environment = fileLoader.load('fileLoader/environment');
}

stage 'Run methods from the loaded content'
helloworld.printHello()
environment.dumpEnvVars()
