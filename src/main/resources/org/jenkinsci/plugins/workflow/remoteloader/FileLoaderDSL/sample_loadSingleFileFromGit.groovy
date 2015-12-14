stage 'Load a file from GitHub'
def helloworld = fileLoader.fromGit('examples/fileLoader/helloworld', 
        'https://github.com/jenkinsci/workflow-remote-loader-plugin.git', 'master', null, '')

stage 'Run method from the loaded file'
helloworld.printHello()
