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
package org.jenkinsci.plugins.workflow.remoteloader.library;

import hudson.model.queue.QueueTaskFuture;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.recipes.LocalData;

import java.io.IOException;

/**
 * Tests for {@link UseRemoteLibraryStep}.
 *
 * @author Robert Sandell &lt;rsandell@cloudbees.com&gt;.
 */
public class UseRemoteLibraryStepTest {

    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Test
    @LocalData
    public void testCall() throws Exception {
        WorkflowJob job = createWorkflow("test", true,
                                         "useRemoteLibrary 'file://"+j.getInstance().getRootDir().getAbsolutePath()+"/testlibrary/.git'\n" +
                                                 "echo 'something'\n" +
                                                 "helloFlow.call()\n" +
                                                 "echo 'good bye'");
        QueueTaskFuture<WorkflowRun> future = job.scheduleBuild2(0);
        j.assertBuildStatusSuccess(future);
        WorkflowRun run = future.get();
        j.assertLogContains("something", run);
        j.assertLogContains("Hello groovy World!", run);
        j.assertLogContains("something", run);
    }

    //@Ignore("Doesn't work to call echo from a classpath class (yet)")
    @Test
    @LocalData
    public void testSay() throws Exception {
        WorkflowJob job = createWorkflow("test", true,
                                         "useRemoteLibrary 'file://"+j.getInstance().getRootDir().getAbsolutePath()+"/testlibrary/.git'\n" +
                                                 "sayHello()");
        QueueTaskFuture<WorkflowRun> future = job.scheduleBuild2(0);
        j.assertBuildStatusSuccess(future);
        j.assertLogContains("Hello groovy World!", future.get());
    }

    protected WorkflowJob createWorkflow(String name, boolean sandbox, String script) throws IOException {
        WorkflowJob job = j.jenkins.createProject(WorkflowJob.class, name);
        job.setDefinition(new CpsFlowDefinition(script, sandbox));
        return job;
    }

}