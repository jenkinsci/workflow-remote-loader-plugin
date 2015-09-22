# Workflow Library Manager Plugin

## Summary

The plugin provides a primitive management of Workflow library scripts.

**Warning!** The project is a **Proof of concept**. The functionality may change before the release (if it happens, of course). Use it on your own risk.

Supported features:
* Library loading from Git and Github (requires an installed Git plugin)
* Multiple library Loading from a local file

## Usage

The plugin adds a global `libManager` variable, which can be used to invoke the Workflow library loading from multiple sources. 

### Available commands

The `libManager` variable provides the following methods:
* `fromGit(String libPath, String repository, String branch, String credentialsId, String labelExpression)` - loading of a single library from Git repository
* `withGit(String repository, String branch, String credentialsId, String labelExpression)` - wrapper for a multiple libs loading from a same Git repo
* `load(String libPath)` - loading of library by a relative path. Also can be used within `withGit` wrapper

Parameters:
* `libPath` - a relative path to the file, ".groovy" extension will be added automatically
* `repository` - string representation of a path to Git repository. Supports all formats supported by [Git Plugin](https://wiki.jenkins-ci.org/display/JENKINS/Git+Plugin)
* `branch` - Optional: Branch to be used (it's also possible to specify labels). Default value: `master`
* `credentialsId` - Optional: Credentials to be used for the Git repo checkout. Default value: `null` (unauthorized access)
* `labelExpression` - Optional: label expression, which specifies a node to be used for checkout. Default value: empty string (runs on any node)

### Library format

The loading behaves similarly to the built-in `load` command, see [Workflow documentation](https://github.com/jenkinsci/workflow-plugin/blob/master/TUTORIAL.md#manual-loading) for more info about library file syntax. Only one file is being loaded by commands from `libManager`. Use static initializers within the Groovy file of the loaded file to load more context from neighbor files.

### Examples

Loading single library from Git:
```groovy
stage 'Load libs from GitHub'
def helloworld = libManager.fromGit('lib/helloworld', 
      'git@github.com:jenkinsci/workflow-samples-lib.git', 'master', null, '')

stage 'Run library contents'
helloworld.printHello()
```

Loading multiple libraries from Git:
```groovy
stage 'Load libs from GitHub'
def environment, helloworld
libManager.withGit('git@github.com:jenkinsci/workflow-samples-lib.git', 'master', null, '') {
        helloworld = libManager.load('lib/helloworld');
        environment = libManager.load('lin/environment');
      }
def helloworld = libs['lib/helloworld'];

stage 'Run library contents'
helloworld.printHello()
environment.dumpEnvVars()
```

## License
[MIT License](http://opensource.org/licenses/MIT)
