# Pipeline Remote File Loader Plugin

## Summary

The plugin simplifies the usage of the shared functionality in [Pipeline](https://github.com/jenkinsci/workflow-plugin) scripts.
It allows to keep the logic in remote files in SCMs and load them on-demand.

Supported features:
* Groovy file loading from Git and Github (requires an installed Git plugin)

## Usage

The plugin adds a global `fileLoader` DSL variable, which provides methods for loading Pipeline objects from remote sources.

### Available methods

The `fileLoader` variable provides the following methods:
* `fromGit(String libPath, String repository, String branch, String credentialsId, String labelExpression)` - loading of a single Groovy file from the specified Git repository
* `withGit(String repository, String branch, String credentialsId, String labelExpression)` - wrapper closure for multiple files loading from a same Git repo
* `fromSVN(String libPath, String repository, String credentialsId, String labelExpression)` - loading of a single Groovy file from the specified SVN repository
* `withSVN(String repository, String credentialsId, String labelExpression)` - wrapper closure for multiple files loading from a same SVN repo

* `load(String libPath)` - loading of an object from a Groovy file specified by the relative path. Also can be used within `withGit()` closure to load multiple objects at once

Parameters:
* `libPath` - a relative path to the file, ".groovy" extension will be added automatically
* `repository` 
    * for Git - string representation of a path to Git repository. Supports all formats supported by [Git Plugin](https://wiki.jenkins-ci.org/display/JENKINS/Git+Plugin)
    * for SVN - string representation of a path to/or inside an SVN repository.
* `branch` - Optional: Branch to be used (it's also possible to specify labels). Default value: `master`
* `credentialsId` - Optional: Credentials to be used for the Git repo checkout. Default value: `null` (unauthorized access)
* `labelExpression` - Optional: label expression, which specifies a node to be used for checkout. Default value: empty string (runs on any node)

### Groovy file format

The loading behaves similarly to the built-in `load` command, see [Pipeline documentation](https://github.com/jenkinsci/workflow-plugin/blob/master/TUTORIAL.md#manual-loading) for more info about library file syntax. Only one file is being loaded by commands from `fileLoader`. Use static initializers within the Groovy file of the loaded file to load more context from neighbor files.

### Examples

Loading a single Groovy file from Git:
```groovy
stage 'Load a file from GitHub'
def helloworld = fileLoader.fromGit('examples/fileLoader/helloworld', 
        'https://github.com/jenkinsci/workflow-remote-loader-plugin.git', 'master', null, '')

stage 'Run method from the loaded file'
helloworld.printHello()
```

Loading multiple files from Git:
```groovy
stage 'Load files from GitHub'
def environment, helloworld
fileLoader.withGit('https://github.com/jenkinsci/workflow-remote-loader-plugin.git', 'master', null, '') {
    helloworld = fileLoader.load('examples/fileLoader/helloworld');
    environment = fileLoader.load('examples/fileLoader/environment');
}

stage 'Run methods from the loaded content'
helloworld.printHello()
environment.dumpEnvVars()
```

## License
[MIT License](http://opensource.org/licenses/MIT)
