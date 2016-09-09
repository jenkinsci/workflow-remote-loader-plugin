stage 'Load a file from GitHub'
def helloworld = fileLoader.fromSVN('fileLoader/helloworld', 
        'https://github.com/jenkinsci/workflow-remote-loader-plugin/trunk/examples@HEAD' )

stage 'Run method from the loaded file'
helloworld.printHello()
