/*
 * The MIT License
 *
 * Copyright (c) 2015 Oleg Nenashev.
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
package org.jenkinsci.plugins.workflow.remoteloader;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.model.Action;
import hudson.model.Node;
import hudson.model.Result;
import hudson.model.queue.QueueTaskFuture;
import hudson.slaves.DumbSlave;
import hudson.slaves.NodeProperty;
import hudson.slaves.RetentionStrategy;
import java.util.Collections;
import org.hamcrest.Matchers;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import static org.junit.Assert.assertThat;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

/**
 * Tests of {@link FileLoaderDSL}.
 * @author Oleg Nenashev
 */
public class FileLoaderDSLTest {
    
    @Rule
    public JenkinsRule j = new JenkinsRule();
    

    @Test
    public void loadSingleFileFromSVN() throws Exception {
        assertSnippet("loadSingleFileFromSVN", false);
    }
    
    @Test
    public void loadSingleFileFromSVN_Sandbox() throws Exception {
        assertSnippet("loadSingleFileFromSVN", true);
    }
    
    @Test
    public void loadMultipleFilesFromSVN() throws Exception {
        assertSnippet("loadMultipleFilesFromSVN", false);
    }


    @Test
    public void loadSingleFileFromGit() throws Exception {
        assertSnippet("loadSingleFileFromGit", false);
    }
    
    @Test
    public void loadSingleFileFromGit_Sandbox() throws Exception {
        assertSnippet("loadSingleFileFromGit", true);
    }
    
    @Test
    public void loadMultipleFilesFromGit() throws Exception {
        assertSnippet("loadMultipleFilesFromGit", false);
    }
    
    @Test
    public void loadMultipleFilesFromGit_Sandbox() throws Exception {
        //TODO: This is a downside of the "environment.groovy" sample. Needs to be adjusted 
        assertSnippetFails("loadMultipleFilesFromGit", true,
                "RejectedAccessException: Scripts not permitted to use method "+
                "org.jenkinsci.plugins.workflow.support.steps.build.RunWrapper build");
    }
    
    private void assertSnippet(@NonNull String snippetName, boolean useSandbox) throws Exception {
        WorkflowJob job = j.jenkins.createProject(WorkflowJob.class, snippetName);
        job.setDefinition(new CpsFlowDefinition(FileLoaderDSL.getSampleSnippet(snippetName), useSandbox));
        
        // Run job
        QueueTaskFuture<WorkflowRun> runFuture = job.scheduleBuild2(0, new Action[0]);
        assertThat("build was actually scheduled", runFuture, Matchers.notNullValue());
        WorkflowRun run = runFuture.get();
        
        j.assertBuildStatus(Result.SUCCESS, run);
    }
    
    private void assertSnippetFails(@NonNull String snippetName, boolean useSandbox,
            @NonNull String expectedMessage) throws Exception {
        WorkflowJob job = j.jenkins.createProject(WorkflowJob.class, snippetName);
        job.setDefinition(new CpsFlowDefinition(FileLoaderDSL.getSampleSnippet(snippetName), useSandbox));
        
        // Run job
        QueueTaskFuture<WorkflowRun> runFuture = job.scheduleBuild2(0, new Action[0]);
        assertThat("build was actually scheduled", runFuture, Matchers.notNullValue());
        WorkflowRun run = runFuture.get();
        
        j.assertBuildStatus(Result.FAILURE, run);
        j.assertLogContains(expectedMessage, run);
    }
}
