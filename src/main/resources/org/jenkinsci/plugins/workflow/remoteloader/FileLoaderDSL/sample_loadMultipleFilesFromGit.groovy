stage 'Load files from GitHub'
def environment, helloworld
fileLoader.withGit('https://github.com/jenkinsci/workflow-remote-loader-plugin.git', 'master', null, '') {
    helloworld = fileLoader.load('examples/fileLoader/helloworld');
    environment = fileLoader.load('examples/fileLoader/environment');
}

stage 'Run methods from the loaded content'
helloworld.printHello()
environment.dumpEnvVars()
