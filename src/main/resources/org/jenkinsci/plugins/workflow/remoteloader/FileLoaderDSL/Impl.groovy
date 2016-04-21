/*
 * The MIT License
 *
 * Copyright (c) 2015 CloudBees, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.jenkinsci.plugins.workflow.remoteloader

import org.jenkinsci.plugins.workflow.cps.CpsScript;

class FileLoaderDSLImpl implements Serializable {
  
  private CpsScript script
  
  private static final String DEFAULT_BRANCH = 'master'
  //TODO: This repo does not exist. It needs something more realistic
  private static final String DEFAULT_REPO_URL = 'git@github.com:jenkinsci/workflow-snippets.git'
  private static final String TMP_FOLDER = 'libLoader'
  

  public FileLoaderDSLImpl(CpsScript script) {
    this.script = script
  }

  public Object fromGit(String libPath, String repoUrl = DEFAULT_REPO_URL, String repoBranch = DEFAULT_BRANCH, 
    String credentialsId = null, labelExpression = '') {
      Object res;
      withGit(repoUrl, repoBranch, credentialsId, labelExpression) {
        res = load(libPath)
      }
      return res
    }
  
  public <V> V withGit(String repoUrl = DEFAULT_REPO_URL, String repoBranch = DEFAULT_BRANCH, 
        String credentialsId = null, labelExpression = '', Closure<V> body) {
    Map<String, Object> loaded = new TreeMap<String, Object>()
    node(labelExpression) {
      withTimestamper {
        script.dir(TMP_FOLDER) {
          // Flush the directory
          script.deleteDir()

          // Checkout
          script.echo "Checking out ${repoUrl}, branch=${repoBranch}"
          script.checkout changelog: false, poll: false, scm: [$class: 'GitSCM', branches: [[name: repoBranch]], 
                          userRemoteConfigs: [[credentialsId: credentialsId, url: repoUrl]]]
          
          // Invoke body in the folder
          body();

          // Flush the directory again
          script.deleteDir()
        }
      }
    }
  }
  
  public Object load(String libPath) {
    def effectiveLibPath = libPath.endsWith(".groovy") ? libPath : libPath + ".groovy";
    script.echo "Loading from ${effectiveLibPath}"
    def lib = script.load "${effectiveLibPath}"
    //TODO:version checks, etc.
    return lib;
  }
  
  private <V> V node(String labelExpression = '', Closure<V> body) {
        // TODO: don't require a new node if the current one fits labels
        //if (script.env.HOME != null) {
        //    // Already inside a node block.
        //    body()
        // } else 
        script.node(labelExpression) {
          body()
        }
    }
    
  private <V> V withTimestamper (Closure<V> body) {
    // TODO: Make this thing optional if the plugin is installed
    //wrap([$class: 'TimestamperBuildWrapper']) {
      body()
    //}
  }
}
